package com.cartoon.api.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import com.cartoon.api.usuario.service.UsuarioService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OficinaRepository oficinaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService service;

    private Oficina oficina;
    private Usuario adminLogado;
    private UsuarioAutenticado autenticadoAdmin;
    private Usuario superAdminLogado;
    private UsuarioAutenticado autenticadoSuperAdmin;

    @BeforeEach
    void setUp() {
        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Centro");
        oficina.setAtivo(true);

        adminLogado = usuario(2, Role.ADMIN, oficina);
        autenticadoAdmin = new UsuarioAutenticado(2L, "admin@teste.com", "ROLE_ADMIN");

        superAdminLogado = usuario(1, Role.SUPERADMIN, null);
        autenticadoSuperAdmin = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");
    }

    private Usuario usuario(Integer id, Role role, Oficina daOficina) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Nome " + id);
        u.setEmail("u" + id + "@teste.com");
        u.setSenha("HASH");
        u.setRole(role);
        u.setOficina(daOficina);
        u.setAtivo(true);
        return u;
    }

    @Test
    void criarMecanico_deveCriarNaOficinaDoAdminComRoleMecanico() {
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));
        when(passwordEncoder.encode("senha1234")).thenReturn("HASH_NOVO");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse resposta = service.criarMecanico(request, autenticadoAdmin);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getRole()).isEqualTo(Role.MECANICO);
        assertThat(salvo.getOficina()).isSameAs(oficina);
        assertThat(salvo.getSenha()).isEqualTo("HASH_NOVO");
        assertThat(resposta.role()).isEqualTo(Role.MECANICO);
        assertThat(resposta.oficinaId()).isEqualTo(1);
        assertThat(resposta.ativo()).isTrue();
    }

    @Test
    void criarMecanico_adminSemOficina_deveLancarAccessDenied() {
        adminLogado.setOficina(null);
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.criarMecanico(request, autenticadoAdmin))
                .isInstanceOf(AccessDeniedException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarMecanico_oficinaInativa_deveLancarConflito() {
        oficina.setAtivo(false);
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.criarMecanico(request, autenticadoAdmin))
                .isInstanceOf(ConflitoException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarAdmin_deveCriarNaOficinaInformadaComRoleAdmin() {
        AdminRequest request = new AdminRequest("Maria", "maria@teste.com", "senha1234", 1);
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(passwordEncoder.encode("senha1234")).thenReturn("HASH_NOVO");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse resposta = service.criarAdmin(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(captor.getValue().getSenha()).isNotEqualTo("senha1234");
        assertThat(resposta.role()).isEqualTo(Role.ADMIN);
        assertThat(resposta.oficinaId()).isEqualTo(1);
    }

    @Test
    void criarAdmin_oficinaInexistente_deveLancarNaoEncontrado() {
        AdminRequest request = new AdminRequest("Maria", "maria@teste.com", "senha1234", 999);
        when(oficinaRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarAdmin(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarAdmin_oficinaInativa_deveLancarConflito() {
        oficina.setAtivo(false);
        AdminRequest request = new AdminRequest("Maria", "maria@teste.com", "senha1234", 1);
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));

        assertThatThrownBy(() -> service.criarAdmin(request))
                .isInstanceOf(ConflitoException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void listar_admin_retornaSoUsuariosDaPropriaOficina() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));
        when(usuarioRepository.findAllByAtivoTrueAndOficinaId(1)).thenReturn(List.of(mecanico));

        List<UsuarioResponse> resultado = service.listar(null, autenticadoAdmin);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(5);
    }

    @Test
    void listar_superAdminSemFiltro_retornaTodosOsAtivos() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(superAdminLogado));
        when(usuarioRepository.findAllByAtivoTrue()).thenReturn(List.of(adminLogado));

        List<UsuarioResponse> resultado = service.listar(null, autenticadoSuperAdmin);

        assertThat(resultado).hasSize(1);
        verify(usuarioRepository, never()).findAllByAtivoTrueAndOficinaId(any());
    }

    @Test
    void listar_superAdminComFiltro_filtraPorOficina() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(superAdminLogado));
        when(usuarioRepository.findAllByAtivoTrueAndOficinaId(1)).thenReturn(List.of(adminLogado));

        List<UsuarioResponse> resultado = service.listar(1, autenticadoSuperAdmin);

        assertThat(resultado).hasSize(1);
        verify(usuarioRepository, never()).findAllByAtivoTrue();
    }

    @Test
    void buscarPorId_adminDaMesmaOficina_retornaUsuario() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        UsuarioResponse resposta = service.buscarPorId(5, autenticadoAdmin);

        assertThat(resposta.id()).isEqualTo(5);
    }

    @Test
    void buscarPorId_adminDeOutraOficina_deveLancarAccessDenied() {
        Oficina outra = new Oficina();
        outra.setId(2);
        Usuario alvo = usuario(7, Role.MECANICO, outra);
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(alvo));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.buscarPorId(7, autenticadoAdmin))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void buscarMeusDados_retornaOUsuarioLogado() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        UsuarioResponse resposta = service.buscarMeusDados(autenticadoAdmin);

        assertThat(resposta.id()).isEqualTo(2);
        assertThat(resposta.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void atualizar_adminEditaMecanicoDaPropriaOficina_semTrocarSenha() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        UsuarioResponse resposta = service.atualizar(5,
                new AtualizarUsuarioRequest("Novo Nome", "novo@teste.com", null), autenticadoAdmin);

        assertThat(mecanico.getNome()).isEqualTo("Novo Nome");
        assertThat(resposta.email()).isEqualTo("novo@teste.com");
        assertThat(mecanico.getSenha()).isEqualTo("HASH");
        verify(usuarioRepository).flush();
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void atualizar_comSenha_gravaHash() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("HASH2");

        service.atualizar(5, new AtualizarUsuarioRequest("Nome", "n@teste.com", "novaSenha123"), autenticadoAdmin);

        assertThat(mecanico.getSenha()).isEqualTo("HASH2");
    }

    @Test
    void atualizar_adminEditaOutroAdmin_deveLancarAccessDenied() {
        Usuario outroAdmin = usuario(8, Role.ADMIN, oficina);
        when(usuarioRepository.findById(8)).thenReturn(Optional.of(outroAdmin));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.atualizar(8,
                new AtualizarUsuarioRequest("X", "x@teste.com", null), autenticadoAdmin))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atualizar_superAdminEditaAdmin_deveAplicarMudancas() {
        Usuario admin = usuario(8, Role.ADMIN, oficina);
        when(usuarioRepository.findById(8)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(superAdminLogado));

        service.atualizar(8, new AtualizarUsuarioRequest("Admin Novo", "an@teste.com", null), autenticadoSuperAdmin);

        assertThat(admin.getNome()).isEqualTo("Admin Novo");
    }

    @Test
    void atualizar_usuarioInativo_deveLancarConflito() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        mecanico.setAtivo(false);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.atualizar(5,
                new AtualizarUsuarioRequest("X", "x@teste.com", null), autenticadoAdmin))
                .isInstanceOf(ConflitoException.class);
    }

    @Test
    void atualizarMeusDados_mecanicoEditaOProprioCadastro() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        UsuarioAutenticado autenticadoMecanico = new UsuarioAutenticado(5L, "m@teste.com", "ROLE_MECANICO");
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));

        UsuarioResponse resposta = service.atualizarMeusDados(
                new AtualizarUsuarioRequest("Meu Novo Nome", "meu@teste.com", null), autenticadoMecanico);

        assertThat(mecanico.getNome()).isEqualTo("Meu Novo Nome");
        assertThat(resposta.role()).isEqualTo(Role.MECANICO);
        verify(usuarioRepository).flush();
    }

    @Test
    void desativar_adminDesativaMecanicoDaPropriaOficina() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        service.desativar(5, autenticadoAdmin);

        assertThat(mecanico.getAtivo()).isFalse();
    }

    @Test
    void desativar_proprioUsuario_deveLancarConflito() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.desativar(2, autenticadoAdmin))
                .isInstanceOf(ConflitoException.class);
        assertThat(adminLogado.getAtivo()).isTrue();
    }

    @Test
    void desativar_superAdminNaoPodeSerDesativadoPorOutro() {
        Usuario outroSuper = usuario(9, Role.SUPERADMIN, null);
        when(usuarioRepository.findById(9)).thenReturn(Optional.of(outroSuper));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(superAdminLogado));

        assertThatThrownBy(() -> service.desativar(9, autenticadoSuperAdmin))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(outroSuper.getAtivo()).isTrue();
    }

    @Test
    void ativar_adminReativaMecanicoDaPropriaOficina() {
        Usuario mecanico = usuario(5, Role.MECANICO, oficina);
        mecanico.setAtivo(false);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(mecanico));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        service.ativar(5, autenticadoAdmin);

        assertThat(mecanico.getAtivo()).isTrue();
    }
}