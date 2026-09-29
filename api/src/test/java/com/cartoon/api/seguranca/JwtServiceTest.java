package com.cartoon.api.seguranca;

import com.cartoon.api.usuario.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "1234567890123456789012345678901234567890";
    private final long expiracaoMs = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expiracaoMs);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido contendo as claims corretas")
    void gerar_DeveGerarTokenValido() {
        String email = "mecanico@cartoon.com";
        Integer id = 1;
        Role role = Role.MECANICO;

        String token = jwtService.gerar(email, id, role);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = jwtService.validar(token);
        assertEquals(email, claims.getSubject());
        assertEquals(id, claims.get("id", Integer.class));
        assertEquals("ROLE_MECANICO", claims.get("role", String.class));
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar token inválido")
    void validar_ComTokenInvalido_DeveLancarExcecao() {
        String tokenInvalido = "token.invalido.fake";

        assertThrows(JwtException.class, () -> jwtService.validar(tokenInvalido));
    }
}
