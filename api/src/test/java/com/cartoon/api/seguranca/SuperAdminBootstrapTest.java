package com.cartoon.api.seguranca;

import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminBootstrapTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Ignora bootstrap quando e-mail ou senha não são informados")
    void ignoraQuandoCredenciaisAusentes() {
        SuperAdminBootstrap semEmail = new SuperAdminBootstrap(
                usuarioRepository, passwordEncoder, "Super Admin", "", "SenhaForte123"
        );
        SuperAdminBootstrap semSenha = new SuperAdminBootstrap(
                usuarioRepository, passwordEncoder, "Super Admin", "admin@cartoon.com.br", "   "
        );

        semEmail.run(new DefaultApplicationArguments());
        semSenha.run(new DefaultApplicationArguments());

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Ignora bootstrap quando já existe usuário SUPERADMIN no banco")
    void ignoraQuandoJaExisteSuperAdmin() {
        when(usuarioRepository.existsByRole(Role.SUPERADMIN)).thenReturn(true);

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                usuarioRepository, passwordEncoder, "Super Admin", "root@cartoon.com.br", "SenhaForte123"
        );
        bootstrap.run(new DefaultApplicationArguments());

        verify(usuarioRepository).existsByRole(Role.SUPERADMIN);
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("Cria o primeiro SUPERADMIN com senha codificada e e-mail normalizado")
    void criaPrimeiroSuperAdminQuandoNaoExiste() {
        when(usuarioRepository.existsByRole(Role.SUPERADMIN)).thenReturn(false);
        when(usuarioRepository.findByEmailIgnoreCase("root@cartoon.com.br")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("SenhaForte123")).thenReturn("$2a$10$hashGerado");

        SuperAdminBootstrap bootstrap = new SuperAdminBootstrap(
                usuarioRepository, passwordEncoder, "  Super Admin Prod  ", "  ROOT@CarToon.com.br ", "SenhaForte123"
        );
        bootstrap.run(new DefaultApplicationArguments());

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertEquals("Super Admin Prod", salvo.getNome());
        assertEquals("root@cartoon.com.br", salvo.getEmail());
        assertEquals("$2a$10$hashGerado", salvo.getSenha());
        assertEquals(Role.SUPERADMIN, salvo.getRole());
        assertNull(salvo.getOficina());
        assertTrue(salvo.getAtivo());
    }
}
