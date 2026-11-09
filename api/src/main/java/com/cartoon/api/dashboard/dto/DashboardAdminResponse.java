package com.cartoon.api.dashboard.dto;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Métricas financeiras e operacionais do dashboard do ADMIN.
 *
 * <p>Regras:
 * <ul>
 *   <li>Faturamento considera apenas OS {@code FINALIZADO}, pela {@code dataFinalizacao}.</li>
 *   <li>Volume e distribuição por status consideram OS abertas no período, pela {@code dataOrcamento}.</li>
 *   <li>Tempo médio é medido em dias, de {@code dataInicio} até {@code dataFinalizacao}.</li>
 * </ul>
 */
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
            /* Variação percentual em relação ao período anterior; null quando o anterior é zero. */
            BigDecimal variacaoPercentual,
            BigDecimal ticketMedio,
            long osFinalizadas
    ) {
    }

    /** Receita líquida do período separada por tipo; o desconto da OS é rateado proporcionalmente. */
    public record ReceitaPorTipo(BigDecimal servicos, BigDecimal pecas) {
    }

    /** {@code chave} é "yyyy-MM-dd" (DIA) ou "yyyy-MM" (MES). */
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
            /* Média de dias entre início e finalização; null se nenhuma OS concluída tiver data de início. */
            BigDecimal tempoMedioDias,
            BigDecimal ticketMedio,
            /* Participação no faturamento do período, em %. */
            BigDecimal participacaoPercentual
    ) {
    }
}
