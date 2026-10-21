package com.cartoon.api.ordemServico.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraOrcamentoTest {

    private final CalculadoraOrcamento calc = new CalculadoraOrcamento();

    @Test
    void servicoDeveSomarUmPorCentoDoValorDoVeiculo() {
        assertEquals(new BigDecimal("350.00"),
                calc.valorServico(new BigDecimal("150.00"), new BigDecimal("20000.00")));
    }

    @Test
    void veiculoMaisCaroDeveGerarServicoMaisCaro() {
        assertEquals(new BigDecimal("3150.00"),
                calc.valorServico(new BigDecimal("150.00"), new BigDecimal("300000.00")));
    }

    @Test
    void deveArredondarParaDuasCasas() {
        assertEquals(new BigDecimal("150.12"),
                calc.valorServico(new BigDecimal("150.00"), new BigDecimal("12.34")));
    }

    @Test
    void naoDeveAceitarValorDoVeiculoNulo() {
        assertThrows(NullPointerException.class,
                () -> calc.valorServico(new BigDecimal("150.00"), null));
    }
}