package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.BloqueioTentativasException;
import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario(Role role, boolean ativo) {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@cartoon.com");
        usuario.setSenha("senhaHash");
        usuario.setRole(role);
        usuario.setAtivo(ativo);
        return usuario;
    }

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar token JWT")
    void login_ComCredenciaisValidas_DeveRetornarToken() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senha123");
        Usuario usuario = usuario(Role.ADMIN, true);

        when(usuarioRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.password(), usuario.getSenha())).thenReturn(true);
        when(jwtService.gerar(usuario.getEmail(), usuario.getId(), usuario.getRole())).thenReturn("token_jwt_mock");

        String token = authService.login(dto);

        assertEquals("token_jwt_mock", token);
        verify(usuarioRepository).findByEmailIgnoreCase(dto.email());
        verify(passwordEncoder).matches(dto.password(), usuario.getSenha());
        verify(jwtService).gerar(usuario.getEmail(), usuario.getId(), usuario.getRole());
        verify(loginAttemptService).registrarSucesso(dto.email());
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando o e-mail não for encontrado")
    void login_ComEmailInexistente_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("inexistente@cartoon.com", "senha123");

        when(usuarioRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.empty());

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verify(usuarioRepository).findByEmailIgnoreCase(dto.email());
        verify(loginAttemptService).registrarFalha(dto.email());
        verify(passwordEncoder).matches(eq(dto.password()), anyString());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando a senha estiver incorreta")
    void login_ComSenhaIncorreta_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senhaErrada");
        Usuario usuario = usuario(Role.ADMIN, true);

        when(usuarioRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.password(), usuario.getSenha())).thenReturn(false);

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verify(usuarioRepository).findByEmailIgnoreCase(dto.email());
        verify(passwordEncoder).matches(dto.password(), usuario.getSenha());
        verify(loginAttemptService).registrarFalha(dto.email());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando o usuário estiver inativo")
    void login_ComUsuarioInativo_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senha123");
        Usuario usuario = usuario(Role.MECANICO, false);

        when(usuarioRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.of(usuario));

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verify(loginAttemptService).registrarFalha(dto.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar BloqueioTentativasException se a conta já estiver bloqueada")
    void login_ContaBloqueada_DeveLancarBloqueioTentativasException() {
        LoginDTO dto = new LoginDTO("bloqueado@cartoon.com", "senha123");
        when(loginAttemptService.estaBloqueado(dto.email())).thenReturn(true);

        BloqueioTentativasException exception = assertThrows(
                BloqueioTentativasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos.", exception.getMessage());
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar BloqueioTentativasException quando a tentativa resultar em bloqueio")
    void login_TentativaResultaEmBloqueio_DeveLancarBloqueioTentativasException() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senhaErrada");
        Usuario usuario = usuario(Role.ADMIN, true);

        when(usuarioRepository.findByEmailIgnoreCase(dto.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.password(), usuario.getSenha())).thenReturn(false);
        when(loginAttemptService.estaBloqueado(dto.email())).thenReturn(false).thenReturn(true);

        BloqueioTentativasException exception = assertThrows(
                BloqueioTentativasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos.", exception.getMessage());
        verify(loginAttemptService).registrarFalha(dto.email());
        verifyNoInteractions(jwtService);
    }
}