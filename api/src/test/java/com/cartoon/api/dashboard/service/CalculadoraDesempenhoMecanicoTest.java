package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.DesempenhoMecanico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraDesempenhoMecanicoTest {

    private final CalculadoraDesempenhoMecanico calculadora = new CalculadoraDesempenhoMecanico();

    private static OsFinalizadaProjecao os(String inicio, String fim, String total, int mecId, String nome) {
        return new OsFinalizadaProjecao(1, inicio == null ? null : LocalDate.parse(inicio), LocalDate.parse(fim),
                new BigDecimal(total), mecId, nome);
    }

    @Test
    @DisplayName("Ordena por faturamento e calcula tempo médio, ticket médio e participação")
    void ranking() {
        List<DesempenhoMecanico> lista = calculadora.calcular(List.of(
                        os("2026-11-01", "2026-11-03", "300", 10, "Ana"),
                        os("2026-11-02", "2026-11-03", "200", 10, "Ana"),
                        os("2026-11-04", "2026-11-04", "800", 11, "Bruno"),
                        os(null, "2026-11-05", "100", 11, "Bruno")),
                List.of(new ContagemMecanicoProjecao(10, "Ana", 2L)),
                new BigDecimal("1400"));

        assertThat(lista).extracting(DesempenhoMecanico::nome).containsExactly("Bruno", "Ana");

        DesempenhoMecanico bruno = lista.get(0);
        assertThat(bruno.faturamento()).isEqualByComparingTo("900.00");
        assertThat(bruno.tempoMedioDias()).isEqualByComparingTo("0.0");
        assertThat(bruno.participacaoPercentual()).isEqualByComparingTo("64.3");

        DesempenhoMecanico ana = lista.get(1);
        assertThat(ana.osEmAndamento()).isEqualTo(2);
        assertThat(ana.tempoMedioDias()).isEqualByComparingTo("1.5");
        assertThat(ana.ticketMedio()).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("Mecânico só com OS em andamento aparece com faturamento zero e sem tempo médio")
    void somenteEmAndamento() {
        List<DesempenhoMecanico> lista = calculadora.calcular(List.of(),
                List.of(new ContagemMecanicoProjecao(12, "Carla", 1L)), BigDecimal.ZERO);

        assertThat(lista).hasSize(1);
        DesempenhoMecanico carla = lista.get(0);
        assertThat(carla.faturamento()).isEqualByComparingTo("0");
        assertThat(carla.osConcluidas()).isZero();
        assertThat(carla.osEmAndamento()).isEqualTo(1);
        assertThat(carla.tempoMedioDias()).isNull();
        assertThat(carla.participacaoPercentual()).isEqualByComparingTo("0");
    }
}
