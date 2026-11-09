package com.cartoon.api.dashboard.service;

import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.Granularidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeriodoDashboardTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 9);

    @Test
    @DisplayName("Sem datas usa o mês atual até hoje e um período anterior com o mesmo número de dias")
    void padraoMesAtual() {
        PeriodoDashboard p = PeriodoDashboard.resolver(null, null, HOJE);

        assertThat(p.inicio()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(p.fim()).isEqualTo(HOJE);
        assertThat(p.inicioAnterior()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(p.fimAnterior()).isEqualTo(LocalDate.of(2026, 10, 31));
        assertThat(p.primeiroMes()).isEqualTo(YearMonth.of(2025, 12));
        assertThat(p.ultimoMes()).isEqualTo(YearMonth.of(2026, 11));
    }

    @Test
    @DisplayName("Granularidade é diária até 62 dias e mensal a partir de 63")
    void granularidade() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        assertThat(PeriodoDashboard.resolver(inicio, inicio.plusDays(61), HOJE).granularidade()).isEqualTo(Granularidade.DIA);
        assertThat(PeriodoDashboard.resolver(inicio, inicio.plusDays(62), HOJE).granularidade()).isEqualTo(Granularidade.MES);
    }

    @Test
    @DisplayName("A consulta começa no que vier antes: período anterior ou início dos 12 meses")
    void inicioConsulta() {
        assertThat(PeriodoDashboard.resolver(null, null, HOJE).inicioConsulta()).isEqualTo(LocalDate.of(2025, 12, 1));
        PeriodoDashboard longo = PeriodoDashboard.resolver(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 11, 9), HOJE);
        assertThat(longo.inicioConsulta()).isEqualTo(longo.inicioAnterior());
    }

    @Test
    @DisplayName("contem e contemNoAnterior incluem os limites e ignoram data nula")
    void contem() {
        PeriodoDashboard p = PeriodoDashboard.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 9), HOJE);

        assertThat(p.contem(LocalDate.of(2026, 11, 1))).isTrue();
        assertThat(p.contem(LocalDate.of(2026, 11, 9))).isTrue();
        assertThat(p.contem(LocalDate.of(2026, 10, 31))).isFalse();
        assertThat(p.contem(null)).isFalse();
        assertThat(p.contemNoAnterior(LocalDate.of(2026, 10, 31))).isTrue();
        assertThat(p.contemNoAnterior(LocalDate.of(2026, 10, 22))).isFalse();
    }

    @Test
    @DisplayName("Rejeita início depois do fim e períodos maiores que 5 anos")
    void validacao() {
        assertThatThrownBy(() -> PeriodoDashboard.resolver(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 1), HOJE))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("data inicial");
        assertThatThrownBy(() -> PeriodoDashboard.resolver(LocalDate.of(2015, 1, 1), LocalDate.of(2026, 11, 1), HOJE))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("5 anos");
    }
}
