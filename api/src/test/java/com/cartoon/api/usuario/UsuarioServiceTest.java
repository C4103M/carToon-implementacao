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
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import com.cartoon.api.usuario.service.UsuarioService;
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
    private UsuarioAutenticado solicitante;

    @BeforeEach
    void setUp() {
        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Centro");
        oficina.setAtivo(true);

        adminLogado = new Usuario();
        adminLogado.setId(2);
        adminLogado.setRole(Role.ADMIN);
        adminLogado.setOficina(oficina);

        solicitante = new UsuarioAutenticado(2L, "admin@teste.com", "ROLE_ADMIN");
    }

    // ---------- criarMecanico ----------

    @Test
    void criarMecanico_deveCriarNaOficinaDoAdminComRoleMecanico() {
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));
        when(passwordEncoder.encode("senha1234")).thenReturn("HASH");
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse resposta = service.criarMecanico(request, solicitante);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getRole()).isEqualTo(Role.MECANICO);
        assertThat(salvo.getOficina()).isSameAs(oficina);
        assertThat(salvo.getSenha()).isEqualTo("HASH");
        assertThat(resposta.role()).isEqualTo(Role.MECANICO);
        assertThat(resposta.oficinaId()).isEqualTo(1);
    }

    @Test
    void criarMecanico_adminSemOficina_deveLancarAccessDenied() {
        adminLogado.setOficina(null);
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.criarMecanico(request, solicitante))
                .isInstanceOf(AccessDeniedException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void criarMecanico_oficinaInativa_deveLancarConflito() {
        oficina.setAtivo(false);
        MecanicoRequest request = new MecanicoRequest("João", "joao@teste.com", "senha1234");
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(adminLogado));

        assertThatThrownBy(() -> service.criarMecanico(request, solicitante))
                .isInstanceOf(ConflitoException.class);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    // ---------- criarAdmin ----------

    @Test
    void criarAdmin_deveCriarNaOficinaInformadaComRoleAdmin() {
        AdminRequest request = new AdminRequest("Maria", "maria@teste.com", "senha1234", 1);
        when(oficinaRepository.findById(1)).thenReturn(Optional.of(oficina));
        when(passwordEncoder.encode("senha1234")).thenReturn("HASH");
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
}