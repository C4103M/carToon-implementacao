package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
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

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.password(), usuario.getSenha())).thenReturn(true);
        when(jwtService.gerar(usuario.getEmail(), usuario.getId(), usuario.getRole())).thenReturn("token_jwt_mock");

        String token = authService.login(dto);

        assertEquals("token_jwt_mock", token);
        verify(usuarioRepository).findByEmail(dto.email());
        verify(passwordEncoder).matches(dto.password(), usuario.getSenha());
        verify(jwtService).gerar(usuario.getEmail(), usuario.getId(), usuario.getRole());
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando o e-mail não for encontrado")
    void login_ComEmailInexistente_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("inexistente@cartoon.com", "senha123");

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verify(usuarioRepository).findByEmail(dto.email());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando a senha estiver incorreta")
    void login_ComSenhaIncorreta_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senhaErrada");
        Usuario usuario = usuario(Role.ADMIN, true);

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(dto.password(), usuario.getSenha())).thenReturn(false);

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verify(usuarioRepository).findByEmail(dto.email());
        verify(passwordEncoder).matches(dto.password(), usuario.getSenha());
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve lançar CredenciaisInvalidasException quando o usuário estiver inativo")
    void login_ComUsuarioInativo_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senha123");
        Usuario usuario = usuario(Role.MECANICO, false);

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(usuario));

        CredenciaisInvalidasException exception = assertThrows(
                CredenciaisInvalidasException.class,
                () -> authService.login(dto)
        );

        assertEquals("Email ou senha incorreto", exception.getMessage());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }
}