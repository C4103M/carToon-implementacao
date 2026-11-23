package com.cartoon.api.servico.models;

import com.cartoon.api.ordemServico.models.ItemServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ServicoModelTest {

    @Test
    @DisplayName("converterParaHorasDecimais - Deve converter LocalTime para horas decimais com precisão")
    void deveConverterLocalTimeParaHorasDecimais() {
        assertEquals(1.5, Servico.converterParaHorasDecimais(LocalTime.of(1, 30)));
        assertEquals(0.75, Servico.converterParaHorasDecimais(LocalTime.of(0, 45)));
        assertEquals(2.0, Servico.converterParaHorasDecimais(LocalTime.of(2, 0)));
        assertNull(Servico.converterParaHorasDecimais(null));
    }

    @Test
    @DisplayName("converterParaLocalTime - Deve converter horas decimais para LocalTime")
    void deveConverterHorasDecimaisParaLocalTime() {
        assertEquals(LocalTime.of(1, 30), Servico.converterParaLocalTime(1.5));
        assertEquals(LocalTime.of(0, 45), Servico.converterParaLocalTime(0.75));
        assertEquals(LocalTime.of(2, 0), Servico.converterParaLocalTime(2.0));
        assertNull(Servico.converterParaLocalTime(null));
    }

    @Test
    @DisplayName("Servico - getQuantidadeHoras e setQuantidadeHoras devem converter automaticamente")
    void deveConverterAutomaticamenteNoServico() {
        Servico s = new Servico();
        s.setTempo(LocalTime.of(1, 30));
        assertEquals(1.5, s.getQuantidadeHoras());

        s.setQuantidadeHoras(2.5);
        assertEquals(LocalTime.of(2, 30), s.getTempo());
        assertEquals(2.5, s.getQuantidadeHoras());
    }

    @Test
    @DisplayName("Servico - Construtores com LocalTime e Double devem inicializar corretamente")
    void deveInicializarPelosConstrutores() {
        Servico s1 = new Servico(1, "Troca de Óleo", 1.5, "Desc");
        assertEquals(LocalTime.of(1, 30), s1.getTempo());
        assertEquals(1.5, s1.getQuantidadeHoras());
        assertThat(s1.getPesoServico()).isEqualByComparingTo(new BigDecimal("0.5"));

        Servico s2 = new Servico(2, "Alinhamento", LocalTime.of(0, 45), "Desc", new BigDecimal("0.0"));
        assertEquals(0.75, s2.getQuantidadeHoras());
        assertThat(s2.getPesoServico()).isEqualByComparingTo(new BigDecimal("0.0"));
    }

    @Test
    @DisplayName("ItemServico - getQuantidadeHoras e setQuantidadeHoras devem converter automaticamente")
    void deveConverterAutomaticamenteNoItemServico() {
        ItemServico item = new ItemServico();
        item.setTempo(LocalTime.of(1, 15));
        assertEquals(1.25, item.getQuantidadeHoras());

        item.setQuantidadeHoras(0.5);
        assertEquals(LocalTime.of(0, 30), item.getTempo());
        assertEquals(0.5, item.getQuantidadeHoras());
    }
}
