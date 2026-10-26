package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.BloqueioTentativasException;
import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private static final String DUMMY_BCRYPT_HASH = "$2a$10$w8T9Q0lB9zDkW1Q7Xz0b6u9U8N1Z3X7O2P5V4W6Y8Z1A2B3C4D5E6";

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public String login(LoginDTO loginDTO) {
        String email = loginDTO.email() == null ? "" : loginDTO.email().trim().toLowerCase();
        if (loginAttemptService.estaBloqueado(email)) {
            throw new BloqueioTentativasException();
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null) {
            // Executa bcrypt em hash dummy para mitigar timing attack de enumeração de e-mails
            passwordEncoder.matches(loginDTO.password() != null ? loginDTO.password() : "", DUMMY_BCRYPT_HASH);
            loginAttemptService.registrarFalha(email);
            if (loginAttemptService.estaBloqueado(email)) {
                throw new BloqueioTentativasException();
            }
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            loginAttemptService.registrarFalha(email);
            if (loginAttemptService.estaBloqueado(email)) {
                throw new BloqueioTentativasException();
            }
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
        }

        if (usuario.getOficina() != null && !Boolean.TRUE.equals(usuario.getOficina().getAtivo()) && usuario.getRole() != Role.SUPERADMIN) {
            loginAttemptService.registrarFalha(email);
            if (loginAttemptService.estaBloqueado(email)) {
                throw new BloqueioTentativasException();
            }
            throw new CredenciaisInvalidasException("Oficina vinculada está inativa.");
        }

        if (!passwordEncoder.matches(loginDTO.password(), usuario.getSenha())) {
            loginAttemptService.registrarFalha(email);
            if (loginAttemptService.estaBloqueado(email)) {
                throw new BloqueioTentativasException();
            }
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
        }

        loginAttemptService.registrarSucesso(email);
        return jwtService.gerar(usuario.getEmail(), usuario.getId(), usuario.getRole());
    }
}
