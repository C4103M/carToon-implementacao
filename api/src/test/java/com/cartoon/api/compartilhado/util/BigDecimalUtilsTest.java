package com.cartoon.api.compartilhado.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalUtilsTest {

    @Test
    @DisplayName("nuloComoZero troca null por zero e mantém os demais valores")
    void nuloComoZero() {
        assertThat(BigDecimalUtils.nuloComoZero(null)).isEqualByComparingTo("0");
        assertThat(BigDecimalUtils.nuloComoZero(new BigDecimal("12.5"))).isEqualByComparingTo("12.5");
    }

    @Test
    @DisplayName("dinheiro arredonda para 2 casas e trata null como zero")
    void dinheiro() {
        assertThat(BigDecimalUtils.dinheiro(new BigDecimal("10.005")).toPlainString()).isEqualTo("10.01");
        assertThat(BigDecimalUtils.dinheiro(null).toPlainString()).isEqualTo("0.00");
        assertThat(BigDecimalUtils.zeroMonetario().toPlainString()).isEqualTo("0.00");
    }

    @Test
    @DisplayName("dividir retorna zero quando a quantidade é zero")
    void dividir() {
        assertThat(BigDecimalUtils.dividir(new BigDecimal("100"), 3).toPlainString()).isEqualTo("33.33");
        assertThat(BigDecimalUtils.dividir(new BigDecimal("100"), 0).toPlainString()).isEqualTo("0.00");
    }

    @Test
    @DisplayName("percentual calcula a participação com 1 casa e retorna zero quando o total é zero")
    void percentual() {
        assertThat(BigDecimalUtils.percentual(new BigDecimal("900"), new BigDecimal("1400")).toPlainString()).isEqualTo("64.3");
        assertThat(BigDecimalUtils.percentual(new BigDecimal("10"), BigDecimal.ZERO).toPlainString()).isEqualTo("0.0");
        assertThat(BigDecimalUtils.percentual(new BigDecimal("10"), null).toPlainString()).isEqualTo("0.0");
    }

    @Test
    @DisplayName("variacaoPercentual compara com o valor anterior e retorna null sem base de comparação")
    void variacaoPercentual() {
        assertThat(BigDecimalUtils.variacaoPercentual(new BigDecimal("800"), new BigDecimal("400"))).isEqualByComparingTo("100.0");
        assertThat(BigDecimalUtils.variacaoPercentual(new BigDecimal("300"), new BigDecimal("400"))).isEqualByComparingTo("-25.0");
        assertThat(BigDecimalUtils.variacaoPercentual(new BigDecimal("300"), BigDecimal.ZERO)).isNull();
        assertThat(BigDecimalUtils.variacaoPercentual(new BigDecimal("300"), null)).isNull();
    }
}
