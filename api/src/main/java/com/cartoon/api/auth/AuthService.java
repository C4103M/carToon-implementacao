package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.BloqueioTentativasException;
import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
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
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            loginAttemptService.registrarFalha(email);
            if (loginAttemptService.estaBloqueado(email)) {
                throw new BloqueioTentativasException();
            }
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
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
