package com.cartoon.api.dashboard.service;

import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Granularidade;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Periodo;
import com.cartoon.api.ordemServico.models.StatusServico;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public record PeriodoDashboard(
        LocalDate inicio,
        LocalDate fim,
        LocalDate inicioAnterior,
        LocalDate fimAnterior,
        Granularidade granularidade,
        YearMonth primeiroMes,
        YearMonth ultimoMes
) {

    static final int LIMITE_DIAS_GRANULARIDADE_DIARIA = 62;
    static final int LIMITE_DIAS_PERIODO = 366 * 5;
    static final int MESES_FATURAMENTO_MENSAL = 12;

    public static PeriodoDashboard resolver(LocalDate inicio, LocalDate fim, LocalDate hoje) {
        LocalDate fimEfetivo = fim != null ? fim : hoje;
        LocalDate inicioEfetivo = inicio != null ? inicio : fimEfetivo.withDayOfMonth(1);
        validar(inicioEfetivo, fimEfetivo);

        long dias = ChronoUnit.DAYS.between(inicioEfetivo, fimEfetivo) + 1;
        Granularidade granularidade = dias > LIMITE_DIAS_GRANULARIDADE_DIARIA ? Granularidade.MES : Granularidade.DIA;
        YearMonth ultimoMes = YearMonth.from(fimEfetivo);

        return new PeriodoDashboard(
                inicioEfetivo,
                fimEfetivo,
                inicioEfetivo.minusDays(dias),
                inicioEfetivo.minusDays(1),
                granularidade,
                ultimoMes.minusMonths(MESES_FATURAMENTO_MENSAL - 1L),
                ultimoMes
        );
    }

    private static void validar(LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data inicial do período não pode ser posterior à data final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > LIMITE_DIAS_PERIODO) {
            throw new RegraDeNegocioException("O período informado não pode ultrapassar 5 anos.");
        }
    }

    public LocalDate inicioConsulta() {
        LocalDate inicioMensal = primeiroMes.atDay(1);
        return inicioAnterior.isBefore(inicioMensal) ? inicioAnterior : inicioMensal;
    }

    public boolean contem(LocalDate data) {
        return entre(data, inicio, fim);
    }

    public boolean contemNoAnterior(LocalDate data) {
        return entre(data, inicioAnterior, fimAnterior);
    }

    public Periodo paraResposta(StatusServico status) {
        return new Periodo(inicio, fim, inicioAnterior, fimAnterior, granularidade, status);
    }

    private static boolean entre(LocalDate data, LocalDate de, LocalDate ate) {
        return data != null && !data.isBefore(de) && !data.isAfter(ate);
    }
}
