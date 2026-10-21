package com.cartoon.api.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    @Test
    @DisplayName("Não deve bloquear e deve contar 0 tentativas inicialmente")
    void estadoInicial_NaoDeveEstarBloqueado() {
        LoginAttemptService service = new LoginAttemptService();
        assertFalse(service.estaBloqueado("usuario@cartoon.com"));
        assertEquals(0, service.getTentativasFalhas("usuario@cartoon.com"));
    }

    @Test
    @DisplayName("Deve registrar tentativas falhas sucessivas sem bloquear antes do limite")
    void registrarFalha_AbaixoDoLimite_NaoDeveBloquear() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "usuario@cartoon.com";

        for (int i = 1; i <= 4; i++) {
            service.registrarFalha(email);
            assertEquals(i, service.getTentativasFalhas(email));
            assertFalse(service.estaBloqueado(email));
        }
    }

    @Test
    @DisplayName("Deve bloquear ao atingir a 5ª tentativa falha consecutiva")
    void registrarFalha_Atingir5Tentativas_DeveBloquear() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "usuario@cartoon.com";

        for (int i = 1; i <= 5; i++) {
            service.registrarFalha(email);
        }

        assertEquals(5, service.getTentativasFalhas(email));
        assertTrue(service.estaBloqueado(email));
    }

    @Test
    @DisplayName("Deve normalizar o e-mail desconsiderando maiúsculas e espaços")
    void registrarFalha_EmailComEspacosEMaiusculas_DeveNormalizar() {
        LoginAttemptService service = new LoginAttemptService();

        service.registrarFalha("  Usuario@Cartoon.COM ");
        service.registrarFalha("usuario@cartoon.com");

        assertEquals(2, service.getTentativasFalhas("USUARIO@cartoon.com"));
    }

    @Test
    @DisplayName("Deve limpar histórico de tentativas ao registrar sucesso")
    void registrarSucesso_DeveResetarTentativasEBloqueio() {
        LoginAttemptService service = new LoginAttemptService();
        String email = "usuario@cartoon.com";

        for (int i = 1; i <= 4; i++) {
            service.registrarFalha(email);
        }
        assertEquals(4, service.getTentativasFalhas(email));

        service.registrarSucesso(email);

        assertEquals(0, service.getTentativasFalhas(email));
        assertFalse(service.estaBloqueado(email));
    }

    @Test
    @DisplayName("Deve desbloquear automaticamente após o tempo de bloqueio de 15 minutos expirar")
    void estaBloqueado_Apos15Minutos_DeveExpirarEDesbloquear() {
        Instant inicio = Instant.parse("2026-10-16T10:00:00Z");
        MutableClock clock = new MutableClock(inicio, ZoneId.of("UTC"));
        LoginAttemptService service = new LoginAttemptService(clock);
        String email = "usuario@cartoon.com";

        // 5 tentativas falhas às 10:00
        for (int i = 1; i <= 5; i++) {
            service.registrarFalha(email);
        }
        assertTrue(service.estaBloqueado(email));

        // Avança 14 minutos e 59 segundos -> ainda bloqueado
        clock.avancar(Duration.ofMinutes(14).plusSeconds(59));
        assertTrue(service.estaBloqueado(email));

        // Avança mais 2 segundos (total 15 min e 1 seg) -> bloqueio expirou
        clock.avancar(Duration.ofSeconds(2));
        assertFalse(service.estaBloqueado(email));
    }

    @Test
    @DisplayName("Não deve lançar erro ao receber e-mail nulo ou vazio")
    void entradasInvalidas_NaoDevemGerarErros() {
        LoginAttemptService service = new LoginAttemptService();

        assertFalse(service.estaBloqueado(null));
        assertFalse(service.estaBloqueado("   "));

        assertDoesNotThrow(() -> service.registrarFalha(null));
        assertDoesNotThrow(() -> service.registrarFalha("   "));

        assertDoesNotThrow(() -> service.registrarSucesso(null));
        assertDoesNotThrow(() -> service.resetar(null));

        assertEquals(0, service.getTentativasFalhas(null));
    }

    private static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        public MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        public void avancar(Duration duracao) {
            this.instant = this.instant.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
