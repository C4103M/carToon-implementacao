package com.cartoon.api.servico;

import com.cartoon.api.servico.dto.deserializer.TempoDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TempoDeserializerTest {

    @Test
    @DisplayName("Deve converter formatos comuns para LocalTime")
    void deveConverterFormatosParaLocalTime() {
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("01:30"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("1:30"));
        assertEquals(LocalTime.of(1, 30, 0), TempoDeserializer.parseTempo("01:30:00"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("1h30"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("1h30m"));
        assertEquals(LocalTime.of(2, 0), TempoDeserializer.parseTempo("2h"));
        assertEquals(LocalTime.of(0, 45), TempoDeserializer.parseTempo("45min"));
        assertEquals(LocalTime.of(0, 30), TempoDeserializer.parseTempo("30m"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("1.5"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("1,5"));
        assertEquals(LocalTime.of(1, 30), TempoDeserializer.parseTempo("2026-10-23T01:30:00"));
    }

    @Test
    @DisplayName("Deve retornar nulo para entrada nula ou vazia")
    void deveRetornarNuloParaVazio() {
        assertNull(TempoDeserializer.parseTempo(null));
        assertNull(TempoDeserializer.parseTempo("   "));
    }
}
