package com.cartoon.api.servico.dto.deserializer;

import com.cartoon.api.servico.models.Servico;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempoDeserializer extends JsonDeserializer<LocalTime> {

    private static final Pattern HORA_MINUTO_PATTERN = Pattern.compile("(\\d+)\\s*[hH]\\s*(\\d+)?(?:[mM])?");
    private static final Pattern MINUTO_ONLY_PATTERN = Pattern.compile("(\\d+)\\s*(?:m|min|minutos)", Pattern.CASE_INSENSITIVE);

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NUMBER_FLOAT || p.currentToken() == JsonToken.VALUE_NUMBER_INT) {
            return Servico.converterParaLocalTime(p.getDoubleValue());
        }

        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return parseTempo(text.trim());
    }

    public static LocalTime parseTempo(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        text = text.trim();

        // 1. Se for ISO DateTime completo (ex: 2026-10-23T01:30:00 ou 2026-10-23T01:30)
        if (text.contains("T")) {
            try {
                return LocalDateTime.parse(text).toLocalTime();
            } catch (Exception ignored) {
            }
        }

        // 2. Se for formato 1h30, 1h30m, 1h, 1H, 2h15
        Matcher mHoraMin = HORA_MINUTO_PATTERN.matcher(text);
        if (mHoraMin.matches()) {
            int h = Integer.parseInt(mHoraMin.group(1));
            int m = mHoraMin.group(2) != null ? Integer.parseInt(mHoraMin.group(2)) : 0;
            if (h > 23 || m > 59) {
                throw new IllegalArgumentException("Duração do serviço excede o limite máximo permitido de 23:59: " + text);
            }
            return LocalTime.of(h, m);
        }

        // 3. Se for formato de minutos apenas: 30m, 45min, 90minutos
        Matcher mMin = MINUTO_ONLY_PATTERN.matcher(text);
        if (mMin.matches()) {
            int totalMin = Integer.parseInt(mMin.group(1));
            int h = totalMin / 60;
            int m = totalMin % 60;
            if (h > 23) {
                throw new IllegalArgumentException("Duração do serviço excede o limite máximo permitido de 23:59: " + text);
            }
            return LocalTime.of(h, m);
        }

        // 4. Se for formato HH:mm ou H:mm ou HH:mm:ss
        if (text.contains(":")) {
            String[] parts = text.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            int s = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
            if (h > 23 || m > 59 || s > 59) {
                throw new IllegalArgumentException("Duração do serviço excede o limite máximo permitido de 23:59: " + text);
            }
            return LocalTime.of(h, m, s);
        }

        // 5. Se for apenas um número em string (ex: "1.5" ou "2" ou "1,5")
        try {
            double val = Double.parseDouble(text.replace(',', '.'));
            int horas = (int) val;
            int minutos = (int) Math.round((val - horas) * 60);
            if (minutos >= 60) {
                horas += minutos / 60;
                minutos = minutos % 60;
            }
            if (horas > 23) {
                throw new IllegalArgumentException("Duração do serviço excede o limite máximo permitido de 23:59: " + text);
            }
            return LocalTime.of(horas, minutos);
        } catch (NumberFormatException ignored) {
        }

        throw new IllegalArgumentException("Formato de tempo inválido: " + text + ". Exemplos válidos: 1:30, 1h30, 01:30:00");
    }
}
