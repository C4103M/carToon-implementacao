package com.cartoon.api.auth;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginAttemptService {

    public static final int MAX_TENTATIVAS = 5;
    public static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);
    public static final int MAX_ENTRIES = 10_000;

    private final Clock clock;
    private final ConcurrentHashMap<String, TentativaInfo> tentativas = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemDefaultZone());
    }

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean estaBloqueado(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String chave = normalizar(email);
        TentativaInfo info = tentativas.get(chave);
        if (info == null) {
            return false;
        }
        if (info.bloqueadoAte != null) {
            if (Instant.now(clock).isBefore(info.bloqueadoAte)) {
                return true;
            } else {
                // Período de bloqueio expirou, limpa o registro
                tentativas.remove(chave);
                return false;
            }
        }
        return false;
    }

    public void registrarFalha(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        evictIfCapacityExceeded();
        String chave = normalizar(email);
        tentativas.compute(chave, (k, info) -> {
            Instant agora = Instant.now(clock);
            if (info == null) {
                info = new TentativaInfo();
            } else if (info.bloqueadoAte != null && agora.isAfter(info.bloqueadoAte)) {
                info = new TentativaInfo();
            }

            int count = info.contagem.incrementAndGet();
            if (count >= MAX_TENTATIVAS) {
                info.bloqueadoAte = agora.plus(TEMPO_BLOQUEIO);
            }
            return info;
        });
    }

    private void evictIfCapacityExceeded() {
        if (tentativas.size() >= MAX_ENTRIES) {
            Instant agora = Instant.now(clock);
            tentativas.entrySet().removeIf(entry -> {
                Instant ate = entry.getValue().bloqueadoAte;
                return ate == null || agora.isAfter(ate);
            });
            if (tentativas.size() >= MAX_ENTRIES) {
                var it = tentativas.keySet().iterator();
                int toRemove = MAX_ENTRIES / 4;
                while (it.hasNext() && toRemove-- > 0) {
                    it.next();
                    it.remove();
                }
            }
        }
    }

    public void registrarSucesso(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        tentativas.remove(normalizar(email));
    }

    public int getTentativasFalhas(String email) {
        if (email == null || email.isBlank()) {
            return 0;
        }
        TentativaInfo info = tentativas.get(normalizar(email));
        return info == null ? 0 : info.contagem.get();
    }

    public void resetar(String email) {
        if (email != null) {
            tentativas.remove(normalizar(email));
        }
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }

    private static class TentativaInfo {
        private final AtomicInteger contagem = new AtomicInteger(0);
        private volatile Instant bloqueadoAte;
    }
}
