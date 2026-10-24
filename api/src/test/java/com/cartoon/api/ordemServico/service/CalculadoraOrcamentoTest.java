package com.cartoon.api.ordemServico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraOrcamentoTest {

    private final CalculadoraOrcamento calc = new CalculadoraOrcamento();

    @Test
    @DisplayName("Deve calcular valor unitário do serviço usando horas x valorHora x (1 + FIPE/100000 x taxaRisco)")
    void deveCalcularValorUnitarioComHorasValorHoraFipeETaxaRisco() {
        // base = 2.0 * 100.00 = 200.00
        // multiplicador = 1 + (50000 / 100000) * 0.10 = 1 + 0.05 = 1.05
        // total = 200.00 * 1.05 = 210.00
        BigDecimal resultado = calc.valorServico(2.0, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("210.00"), resultado);
    }

    @Test
    @DisplayName("Quando taxaRisco for zero ou nula, deve retornar apenas horas x valorHora")
    void quandoTaxaRiscoZeroOuNulaDeveRetornarHorasVezesValorHora() {
        BigDecimal resultadoZero = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("150.00"), resultadoZero);

        BigDecimal resultadoNulo = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"), null);
        assertEquals(new BigDecimal("150.00"), resultadoNulo);

        BigDecimal resultadoSemTaxa = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"));
        assertEquals(new BigDecimal("150.00"), resultadoSemTaxa);
    }

    @Test
    @DisplayName("Veículo com FIPE mais cara deve gerar serviço proporcionalmente mais caro pela taxa de risco")
    void veiculoMaisCaroDeveGerarServicoMaisCaro() {
        BigDecimal v1 = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("100000.00"), new BigDecimal("0.10"));
        BigDecimal v2 = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("200000.00"), new BigDecimal("0.10"));

        assertEquals(new BigDecimal("110.00"), v1);
        assertEquals(new BigDecimal("120.00"), v2);
    }

    @Test
    @DisplayName("Deve arredondar o resultado para duas casas decimais com HALF_UP")
    void deveArredondarParaDuasCasas() {
        // base = 1.0 * 100.00 = 100.00
        // fipe = 33333.33 -> fator = 0.33333330
        // taxa = 0.1 -> adicional = 0.03333333 -> multiplicador = 1.03333333
        // 100 * 1.03333333 = 103.33
        BigDecimal resultado = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("33333.33"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("103.33"), resultado);
    }

    @Test
    @DisplayName("Não deve aceitar parâmetros obrigatórios nulos")
    void naoDeveAceitarParametrosObrigatoriosNulos() {
        assertThrows(NullPointerException.class,
                () -> calc.valorServico((Double) null, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico(1.0, null, new BigDecimal("50000.00"), new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico(1.0, new BigDecimal("100.00"), null, new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico((java.time.LocalTime) null, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10")));
    }

    @Test
    @DisplayName("Deve calcular valor do serviço a partir de LocalTime convertendo para decimal")
    void deveCalcularValorServicoLocalTime() {
        // 01:30 -> 1.5 horas: 1.5 * 100.00 * (1 + 0.05) = 157.50
        BigDecimal resultado = calc.valorServico(
                java.time.LocalTime.of(1, 30),
                new BigDecimal("100.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.10")
        );
        assertEquals(new BigDecimal("157.50"), resultado);

        // 00:45 -> 0.75 horas: 0.75 * 100 * 1.05 = 78.75
        BigDecimal resultado45 = calc.valorServico(
                java.time.LocalTime.of(0, 45),
                new BigDecimal("100.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.10")
        );
        assertEquals(new BigDecimal("78.75"), resultado45);
    }
}
