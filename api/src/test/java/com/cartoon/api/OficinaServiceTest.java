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
        when(oficinaRepository.save(oficina)).thenReturn(oficina);
        when(mapper.toResponse(oficina)).thenReturn(response);

        OficinaResponse resultado = service.criar(request);

        assertThat(resultado.id()).isEqualTo(1);
        assertThat(resultado.ativo()).isTrue();
        verify(oficinaRepository).save(oficina);
    }

    @Test
    void criar_comNomeJaExistente_deveLancarConflitoENaoSalvar() {
        OficinaRequest request = new OficinaRequest("Oficina Centro", "Rua X", "11988887777");
        when(oficinaRepository.existsByNome("Oficina Centro")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("nome");
        verify(oficinaRepository, never()).save(any());
    }

    @Test
    void criar_comTelefoneJaExistente_deveLancarConflitoENaoSalvar() {
        OficinaRequest request = new OficinaRequest("Nova Oficina", "Rua X", "11988887777");
        when(mapper.normalizarTelefone("11988887777")).thenReturn("11988887777");
        when(oficinaRepository.existsByTelefone("11988887777")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflitoException.class)
                .hasMessageContaining("telefone");
        verify(oficinaRepository, never()).save(any());
    }

    @Test
    void criar_comTelefoneFormatado_deveCompararComONumeroNormalizado() {
        OficinaRequest request = new OficinaRequest("Nova Oficina", "Rua X", "(11) 98888-7777");
        when(mapper.normalizarTelefone("(11) 98888-7777")).thenReturn("11988887777");
        when(oficinaRepository.existsByTelefone("11988887777")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ConflitoException.class);
        verify(oficinaRepository, never()).save(any());
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
    void atualizar_deveAplicarMudancasNaEntidadeExistente() {
        OficinaRequest request = new OficinaRequest("Novo Nome", "Av. Brasil, 1500", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        service.atualizar(1, request);

        verify(mapper).updateEntity(oficina, request);
    }

    @Test
    void atualizar_comNomeDeOutraOficina_deveLancarConflito() {
        OficinaRequest request = new OficinaRequest("Outra Oficina", "Rua X", "11988887777");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(oficinaRepository.existsByNomeAndIdNot("Outra Oficina", 1)).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(1, request))
                .isInstanceOf(ConflitoException.class);
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void atualizar_mantendoOProprioNome_naoDeveConsiderarDuplicado() {
        OficinaRequest request = new OficinaRequest("Oficina Centro", "Rua Nova", "11988887777");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        service.atualizar(1, request);

        verify(oficinaRepository).existsByNomeAndIdNot("Oficina Centro", 1);
        verify(oficinaRepository, never()).existsByNome(any());
        verify(mapper).updateEntity(oficina, request);
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
    void desativar_quandoNaoExiste_deveLancarExcecaoENaoSalvar() {
        when(oficinaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(999))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(oficinaRepository, never()).save(any());
    }

    @Test
    void ativar_deveMarcarAtivoComoTrue() {
        oficina.setAtivo(false);
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        service.ativar(1);

        assertThat(oficina.getAtivo()).isTrue();
    }
}