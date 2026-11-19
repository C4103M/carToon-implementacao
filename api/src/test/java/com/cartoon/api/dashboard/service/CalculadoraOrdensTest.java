package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.PontoQuantidade;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.QuantidadePorStatus;
import com.cartoon.api.ordemServico.models.StatusServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class CalculadoraOrdensTest {

    private final CalculadoraOrdens calculadora = new CalculadoraOrdens();
    private final PeriodoDashboard periodo =
            PeriodoDashboard.resolver(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 9));
    private final List<OsAberturaProjecao> abertas = List.of(
            new OsAberturaProjecao(LocalDate.of(2026, 11, 1), StatusServico.PENDENTE),
            new OsAberturaProjecao(LocalDate.of(2026, 11, 1), StatusServico.FINALIZADO),
            new OsAberturaProjecao(LocalDate.of(2026, 11, 3), StatusServico.PENDENTE));

    @Test
    @DisplayName("Volume sem filtro conta todas as OS abertas por dia")
    void volumeSemFiltro() {
        assertThat(calculadora.volume(abertas, null, periodo))
                .extracting(PontoQuantidade::chave, PontoQuantidade::quantidade)
                .containsExactly(tuple("2026-11-01", 2L), tuple("2026-11-02", 0L), tuple("2026-11-03", 1L));
    }

    @Test
    @DisplayName("Volume com filtro conta apenas o status escolhido")
    void volumeComFiltro() {
        assertThat(calculadora.volume(abertas, StatusServico.PENDENTE, periodo))
                .extracting(PontoQuantidade::quantidade)
                .containsExactly(1L, 0L, 1L);
    }

    @Test
    @DisplayName("Distribuição por status traz todos os status, inclusive os sem OS")
    void porStatus() {
        assertThat(calculadora.porStatus(abertas))
                .extracting(QuantidadePorStatus::status, QuantidadePorStatus::quantidade)
                .containsExactly(
                        tuple(StatusServico.PENDENTE, 2L),
                        tuple(StatusServico.ACEITO, 0L),
                        tuple(StatusServico.REJEITADO, 0L),
                        tuple(StatusServico.EM_ANDAMENTO, 0L),
                        tuple(StatusServico.FINALIZADO, 1L));
    }
}
