package com.cartoon.api.seguranca;

import com.cartoon.api.usuario.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiracaoMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiracao-ms}") long expiracaoMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerar(String email, Integer id,Role role) {
        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("role", "ROLE_" + role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiracaoMs))
                .signWith(key)
                .compact();
    }

    public Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

}
