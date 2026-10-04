package com.cartoon.api.seguranca;

import com.cartoon.api.auth.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;

import java.io.IOException;

@Log4j2
@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtCookieFilter(JwtService jwtService) {
        this.jwtService = jwtService;
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
