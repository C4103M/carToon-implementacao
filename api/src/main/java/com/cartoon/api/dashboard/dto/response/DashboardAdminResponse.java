package com.cartoon.api.dashboard.dto.response;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DashboardAdminResponse(
        Periodo periodo,
        Faturamento faturamento,
        ReceitaPorTipo receitaPorTipo,
        List<PontoValor> faturamentoMensal,
        List<PontoValor> evolucaoFaturamento,
        List<PontoQuantidade> volumeOrdens,
        List<QuantidadePorStatus> ordensPorStatus,
        List<DesempenhoMecanico> mecanicos
) {

    public enum Granularidade { DIA, MES }

    public record Periodo(
            LocalDate inicio,
            LocalDate fim,
            LocalDate inicioAnterior,
            LocalDate fimAnterior,
            Granularidade granularidade,
            StatusServico status
    ) {
    }

    public record Faturamento(
            BigDecimal totalAcumulado,
            BigDecimal periodo,
            BigDecimal periodoAnterior,
            BigDecimal variacaoPercentual,
            BigDecimal ticketMedio,
            long osFinalizadas
    ) {
    }

    public record ReceitaPorTipo(BigDecimal servicos, BigDecimal pecas) {
    }

    public record PontoValor(String chave, BigDecimal valor) {
    }

    public record PontoQuantidade(String chave, long quantidade) {
    }

    public record QuantidadePorStatus(StatusServico status, long quantidade) {
    }

    public record DesempenhoMecanico(
            Integer mecanicoId,
            String nome,
            BigDecimal faturamento,
            long osConcluidas,
            long osEmAndamento,
            BigDecimal tempoMedioDias,
            BigDecimal ticketMedio,
            BigDecimal participacaoPercentual
    ) {
    }
}
