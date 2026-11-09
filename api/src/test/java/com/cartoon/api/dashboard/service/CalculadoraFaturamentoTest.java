package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Faturamento;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.PontoValor;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.ReceitaPorTipo;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraFaturamentoTest {

    private final CalculadoraFaturamento calculadora = new CalculadoraFaturamento();
    private final PeriodoDashboard novembro =
            PeriodoDashboard.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 9));

    private static OsFinalizadaProjecao os(int id, String fim, String total) {
        return new OsFinalizadaProjecao(id, null, LocalDate.parse(fim), new BigDecimal(total), 1, "Ana");
    }

    @Test
    @DisplayName("Resumo traz período, anterior, variação, ticket médio e quantidade")
    void resumo() {
        Faturamento f = calculadora.resumo(new BigDecimal("5000"),
                List.of(os(1, "2026-11-02", "300"), os(2, "2026-11-03", "500")),
                List.of(os(3, "2026-10-25", "400")));

        assertThat(f.totalAcumulado()).isEqualByComparingTo("5000.00");
        assertThat(f.periodo()).isEqualByComparingTo("800.00");
        assertThat(f.periodoAnterior()).isEqualByComparingTo("400.00");
        assertThat(f.variacaoPercentual()).isEqualByComparingTo("100.0");
        assertThat(f.ticketMedio()).isEqualByComparingTo("400.00");
        assertThat(f.osFinalizadas()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resumo sem OS e sem total acumulado retorna zeros e variação nula")
    void resumoVazio() {
        Faturamento f = calculadora.resumo(null, List.of(), List.of());

        assertThat(f.totalAcumulado()).isEqualByComparingTo("0");
        assertThat(f.ticketMedio()).isEqualByComparingTo("0");
        assertThat(f.variacaoPercentual()).isNull();
    }

    @Test
    @DisplayName("Receita por tipo rateia o desconto pelo valor bruto e manda OS sem itens para serviços")
    void receitaPorTipo() {
        ReceitaPorTipo r = calculadora.receitaPorTipo(
                List.of(os(1, "2026-11-02", "900"), os(2, "2026-11-02", "50")),
                List.of(new SomaPorOsProjecao(1, new BigDecimal("600"))),
                List.of(new SomaPorOsProjecao(1, new BigDecimal("400"))));

        assertThat(r.pecas()).isEqualByComparingTo("540.00");
        assertThat(r.servicos()).isEqualByComparingTo("410.00");
    }

    @Test
    @DisplayName("Faturamento mensal cobre 12 meses e ignora OS fora da janela")
    void mensal() {
        List<PontoValor> pontos = calculadora.mensal(List.of(
                os(1, "2026-11-05", "100"),
                os(2, "2026-11-20", "999"),
                os(3, "2026-06-10", "250"),
                os(4, "2025-10-31", "777")), novembro);

        assertThat(pontos).hasSize(12);
        assertThat(pontos.get(0).chave()).isEqualTo("2025-12");
        assertThat(valor(pontos, "2026-11")).isEqualByComparingTo("100.00");
        assertThat(valor(pontos, "2026-06")).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("Evolução tem um ponto por dia do período, com zero nos dias sem faturamento")
    void evolucao() {
        List<PontoValor> pontos = calculadora.evolucao(
                List.of(os(1, "2026-11-03", "300"), os(2, "2026-11-03", "200")), novembro);

        assertThat(pontos).hasSize(9);
        assertThat(valor(pontos, "2026-11-03")).isEqualByComparingTo("500.00");
        assertThat(valor(pontos, "2026-11-04")).isEqualByComparingTo("0");
    }

    private static BigDecimal valor(List<PontoValor> pontos, String chave) {
        return pontos.stream().filter(p -> p.chave().equals(chave)).findFirst().orElseThrow().valor();
    }
}
