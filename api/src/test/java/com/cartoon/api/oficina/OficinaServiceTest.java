package com.cartoon.api.oficina;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class OficinaServiceTest {

    @Mock
    private OficinaRepository oficinaRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private OficinaMapper mapper;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private OficinaService service;

    private Oficina oficina;
    private OficinaResponse response;
    private UsuarioAutenticado superAdmin;
    private UsuarioAutenticado admin;

    @BeforeEach
    void setUp() {
        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Centro");
        oficina.setEndereco("Rua das Flores, 200");
        oficina.setTelefone("11988887777");
        oficina.setAtivo(true);

        response = new OficinaResponse(1, "Oficina Centro", "Rua das Flores, 200", "11988887777", true);
        superAdmin = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");
        admin = new UsuarioAutenticado(2L, "admin@teste.com", "ROLE_ADMIN");
    }

    private Usuario usuarioComOficina(Oficina daOficina) {
        Usuario usuario = new Usuario();
        usuario.setOficina(daOficina);
        return usuario;
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
    void atualizar_superAdmin_editaQualquerOficinaSemConsultarUsuario() {
        OficinaRequest request = new OficinaRequest("Novo Nome", "Av. Brasil, 1500", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        service.atualizar(1, request, superAdmin);

        verify(mapper).updateEntity(oficina, request);
        verify(oficinaRepository).flush();
        verifyNoInteractions(usuarioService);
    }

    @Test
    void atualizar_adminDaPropriaOficina_deveAplicarMudancas() {
        OficinaRequest request = new OficinaRequest("Novo Nome", "Av. Brasil, 1500", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(usuarioService.buscarEntidade(2)).thenReturn(usuarioComOficina(oficina));
        when(mapper.toResponse(oficina)).thenReturn(response);

        service.atualizar(1, request, admin);

        verify(mapper).updateEntity(oficina, request);
        verify(oficinaRepository).flush();
    }

    @Test
    void atualizar_adminDeOutraOficina_deveLancarAccessDenied() {
        Oficina outra = new Oficina();
        outra.setId(2);
        OficinaRequest request = new OficinaRequest("Novo Nome", "Rua X", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(usuarioService.buscarEntidade(2)).thenReturn(usuarioComOficina(outra));

        assertThatThrownBy(() -> service.atualizar(1, request, admin))
                .isInstanceOf(AccessDeniedException.class);
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void atualizar_adminSemOficina_deveLancarAccessDenied() {
        OficinaRequest request = new OficinaRequest("Novo Nome", "Rua X", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(usuarioService.buscarEntidade(2)).thenReturn(usuarioComOficina(null));

        assertThatThrownBy(() -> service.atualizar(1, request, admin))
                .isInstanceOf(AccessDeniedException.class);
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void atualizar_oficinaInativa_deveLancarConflito() {
        oficina.setAtivo(false);
        OficinaRequest request = new OficinaRequest("Novo Nome", "Rua X", "11977776666");
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        assertThatThrownBy(() -> service.atualizar(1, request, superAdmin))
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