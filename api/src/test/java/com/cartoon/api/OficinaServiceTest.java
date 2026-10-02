package com.cartoon.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OficinaServiceTest {

    @Mock
    private OficinaRepository oficinaRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OficinaMapper mapper;

    @InjectMocks
    private OficinaService service;

    private Oficina oficina;
    private OficinaResponse response;

    @BeforeEach
    void setUp() {
        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Centro");
        oficina.setEndereco("Rua das Flores, 200");
        oficina.setTelefone("11988887777");
        oficina.setAtivo(true);

        response = new OficinaResponse(1, "Oficina Centro", "Rua das Flores, 200", "11988887777", true);
    }

    // ---------- criar ----------

    @Test
    void criar_deveSalvarERetornarResponse() {
        OficinaRequest request = new OficinaRequest("Oficina Centro", "Rua das Flores, 200", "11988887777");
        when(mapper.toEntity(request)).thenReturn(oficina);
        when(oficinaRepository.saveAndFlush(oficina)).thenReturn(oficina);
        when(mapper.toResponse(oficina)).thenReturn(response);

        OficinaResponse resultado = service.criar(request);

        assertThat(resultado.id()).isEqualTo(1);
        assertThat(resultado.ativo()).isTrue();
        verify(oficinaRepository).saveAndFlush(oficina);
    }

    // ---------- listar e buscar ----------

    @Test
    void listar_deveBuscarApenasAtivas() {
        when(oficinaRepository.findAllByAtivoTrue()).thenReturn(List.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        List<OficinaResponse> resultado = service.listar();

        assertThat(resultado).hasSize(1);
        verify(oficinaRepository).findAllByAtivoTrue();
        verify(oficinaRepository, never()).findAll();
    }

    @Test
    void buscarPorId_quandoExiste_deveRetornarResponse() {
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        OficinaResponse resultado = service.buscarPorId(1);

        assertThat(resultado.nome()).isEqualTo("Oficina Centro");
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveLancarExcecao() {
        when(oficinaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- atualizar ----------

    @Test
    void atualizar_deveAplicarMudancasEForcarFlush() {
        OficinaRequest request = new OficinaRequest("Novo Nome", "Av. Brasil, 1500", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        service.atualizar(1, request);

        verify(mapper).updateEntity(oficina, request);
        verify(oficinaRepository).flush();
    }

    @Test
    void atualizar_oficinaInativa_deveLancarConflito() {
        oficina.setAtivo(false);
        OficinaRequest request = new OficinaRequest("Novo Nome", "Rua X", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        assertThatThrownBy(() -> service.atualizar(1, request))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("inativa");
        verify(mapper, never()).updateEntity(any(), any());
    }

    // ---------- ativar e desativar ----------

    @Test
    void desativar_deveMarcarAtivoComoFalse() {
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        service.desativar(1);

        assertThat(oficina.getAtivo()).isFalse();
    }

    @Test
    void desativar_comOrdensEmAberto_deveLancarConflitoEManterAtiva() {
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(ordemServicoRepository.existsByOficinaIdAndStatusServicoIn(eq(1), any())).thenReturn(true);

        assertThatThrownBy(() -> service.desativar(1))
                .isInstanceOf(ConflitoException.class);
        assertThat(oficina.getAtivo()).isTrue();
    }

    @Test
    void desativar_quandoNaoExiste_deveLancarExcecao() {
        when(oficinaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(999))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void ativar_deveMarcarAtivoComoTrue() {
        oficina.setAtivo(false);
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        service.ativar(1);

        assertThat(oficina.getAtivo()).isTrue();
    }
}