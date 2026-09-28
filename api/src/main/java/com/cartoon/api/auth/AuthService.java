package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.buf.UEncoder;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtService jtwService;
    private final PasswordEncoder passwordEncoder;



    public String login(LoginDTO loginDTO) {
        Usuario usuario = usuarioRepository.findByEmail(loginDTO.email()).orElse(null);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Email ou senha incorreto" );
        }









    }

}
