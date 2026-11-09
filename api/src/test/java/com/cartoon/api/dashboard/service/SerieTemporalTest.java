package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Granularidade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SerieTemporalTest {

    @Test
    @DisplayName("Gera uma chave por dia, incluindo início e fim")
    void chavesPorDia() {
        assertThat(SerieTemporal.chaves(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2), Granularidade.DIA))
                .containsExactly("2026-10-30", "2026-10-31", "2026-11-01", "2026-11-02");
    }

    @Test
    @DisplayName("Gera uma chave por mês, atravessando a virada do ano")
    void chavesPorMes() {
        assertThat(SerieTemporal.chaves(LocalDate.of(2025, 11, 15), LocalDate.of(2026, 2, 3), Granularidade.MES))
                .containsExactly("2025-11", "2025-12", "2026-01", "2026-02");
    }

    @Test
    @DisplayName("Converte uma data na chave da granularidade e trata data nula")
    void chave() {
        LocalDate data = LocalDate.of(2026, 3, 7);
        assertThat(SerieTemporal.chave(data, Granularidade.DIA)).isEqualTo("2026-03-07");
        assertThat(SerieTemporal.chave(data, Granularidade.MES)).isEqualTo("2026-03");
        assertThat(SerieTemporal.chave(null, Granularidade.DIA)).isEmpty();
    }
}
