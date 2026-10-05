package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public String login(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.email()).orElse(null);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
        }
        if(!passwordEncoder.matches(loginDTO.password(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("Email ou senha incorreto");
        }
        return jwtService.gerar(usuario.getEmail(), usuario.getId(), usuario.getRole());
    }

}
