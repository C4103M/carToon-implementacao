package com.cartoon.api.ordemServico.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DescontoTest {

    @Test
    @DisplayName("calcularValorDesconto - Deve calcular corretamente percentuais até 15%")
    void calcularValorDesconto_DeveCalcularValoresCorretos() {
        BigDecimal bruto = new BigDecimal("1000.00");

        assertEquals(new BigDecimal("0.00"), Desconto.SEM_DESCONTO.calcularValorDesconto(bruto));
        assertEquals(new BigDecimal("50.00"), Desconto.CINCO_PORCENTO.calcularValorDesconto(bruto));
        assertEquals(new BigDecimal("100.00"), Desconto.DEZ_PORCENTO.calcularValorDesconto(bruto));
        assertEquals(new BigDecimal("150.00"), Desconto.QUINZE_PORCENTO.calcularValorDesconto(bruto));
    }

    @Test
    @DisplayName("calcularValorDesconto - Quando valor bruto for nulo ou zero, deve retornar zero")
    void calcularValorDesconto_ComValorNuloOuZero_DeveRetornarZero() {
        assertEquals(new BigDecimal("0.00"), Desconto.DEZ_PORCENTO.calcularValorDesconto(null));
        assertEquals(new BigDecimal("0.00"), Desconto.DEZ_PORCENTO.calcularValorDesconto(BigDecimal.ZERO));
        assertEquals(new BigDecimal("0.00"), Desconto.DEZ_PORCENTO.calcularValorDesconto(new BigDecimal("-50.00")));
    }

    @Test
    @DisplayName("fromString - Deve desserializar variações válidas")
    void fromString_DeveDesserializarVariacoesValidas() {
        assertEquals(Desconto.SEM_DESCONTO, Desconto.fromString(null));
        assertEquals(Desconto.SEM_DESCONTO, Desconto.fromString(""));
        assertEquals(Desconto.SEM_DESCONTO, Desconto.fromString("0"));
        assertEquals(Desconto.SEM_DESCONTO, Desconto.fromString("0%"));
        assertEquals(Desconto.SEM_DESCONTO, Desconto.fromString("SEM_DESCONTO"));

        assertEquals(Desconto.CINCO_PORCENTO, Desconto.fromString("5"));
        assertEquals(Desconto.CINCO_PORCENTO, Desconto.fromString("5%"));
        assertEquals(Desconto.CINCO_PORCENTO, Desconto.fromString("CINCO"));
        assertEquals(Desconto.CINCO_PORCENTO, Desconto.fromString("CINCO_PORCENTO"));

        assertEquals(Desconto.DEZ_PORCENTO, Desconto.fromString("10"));
        assertEquals(Desconto.DEZ_PORCENTO, Desconto.fromString("10%"));
        assertEquals(Desconto.DEZ_PORCENTO, Desconto.fromString("DEZ"));
        assertEquals(Desconto.DEZ_PORCENTO, Desconto.fromString("DEZ_PORCENTO"));

        assertEquals(Desconto.QUINZE_PORCENTO, Desconto.fromString("15"));
        assertEquals(Desconto.QUINZE_PORCENTO, Desconto.fromString("15%"));
        assertEquals(Desconto.QUINZE_PORCENTO, Desconto.fromString("QUINZE"));
        assertEquals(Desconto.QUINZE_PORCENTO, Desconto.fromString("QUINZE_PORCENTO"));
    }

    @Test
    @DisplayName("fromString - Descontos acima de 15% ou inválidos devem lançar IllegalArgumentException")
    void fromString_DescontoInvalidoOuMaiorQue15_DeveLancarExcecao() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> Desconto.fromString("20"));
        assertTrue(e1.getMessage().contains("15%"));

        Exception e2 = assertThrows(IllegalArgumentException.class, () -> Desconto.fromString("VINTE"));
        assertTrue(e2.getMessage().contains("Desconto inválido"));
    }
}
