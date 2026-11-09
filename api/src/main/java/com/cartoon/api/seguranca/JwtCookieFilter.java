package com.cartoon.api.seguranca;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtCookieFilter(JwtService jwtService, @Autowired(required = false) UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extrairToken(request);
        if (token != null) {
            try {
                Claims claims = jwtService.validar(token);
                Number idNumber = claims.get("id", Number.class);
                Long id = idNumber != null ? idNumber.longValue() : null;
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                if (id != null && usuarioRepository != null) {
                    Usuario usuario = usuarioRepository.findById(id.intValue()).orElse(null);
                    if (usuario == null || !Boolean.TRUE.equals(usuario.getAtivo())) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    if (usuario.getOficina() != null && !Boolean.TRUE.equals(usuario.getOficina().getAtivo()) && usuario.getRole() != Role.SUPERADMIN) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    if (usuario.getRole() != null) {
                        role = "ROLE_" + usuario.getRole().name();
                    }
                    email = usuario.getEmail();
                }

                var usuarioAutenticado = new UsuarioAutenticado(id, email, role);
                var authority = new SimpleGrantedAuthority(role);
                var auth = new UsernamePasswordAuthenticationToken(
                        usuarioAutenticado, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ignored) {
                log.debug("\n\n\n\nToken JWT inválido ou expirado: {}\n\n\n\n", ignored.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
