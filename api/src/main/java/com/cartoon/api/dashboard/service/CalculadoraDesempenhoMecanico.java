package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.projecao.ContagemMecanicoProjecao;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.response.DashboardAdminResponse.DesempenhoMecanico;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cartoon.api.compartilhado.util.BigDecimalUtils.dinheiro;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.dividir;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.nuloComoZero;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.percentual;

@Component
public class CalculadoraDesempenhoMecanico {

    private static final Comparator<DesempenhoMecanico> RANKING =
            Comparator.comparing(DesempenhoMecanico::faturamento).reversed()
                    .thenComparing(Comparator.comparingLong(DesempenhoMecanico::osConcluidas).reversed())
                    .thenComparing(DesempenhoMecanico::nome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    public List<DesempenhoMecanico> calcular(List<OsFinalizadaProjecao> finalizadasNoPeriodo,
                                             List<ContagemMecanicoProjecao> emAndamento,
                                             BigDecimal faturamentoPeriodo) {
        Map<Integer, Acumulador> porMecanico = new LinkedHashMap<>();
        for (OsFinalizadaProjecao os : finalizadasNoPeriodo) {
            porMecanico.computeIfAbsent(os.mecanicoId(), id -> new Acumulador(id, os.mecanicoNome()))
                    .registrarConcluida(os);
        }
        for (ContagemMecanicoProjecao c : emAndamento) {
            porMecanico.computeIfAbsent(c.mecanicoId(), id -> new Acumulador(id, c.mecanicoNome()))
                    .emAndamento = c.quantidade() != null ? c.quantidade() : 0L;
        }
        return porMecanico.values().stream()
                .map(acc -> acc.paraDesempenho(faturamentoPeriodo))
                .sorted(RANKING)
                .toList();
    }

    private static final class Acumulador {
        private final Integer id;
        private final String nome;
        private BigDecimal faturamento = BigDecimal.ZERO;
        private long concluidas;
        private long emAndamento;
        private long somaDias;
        private long comTempo;

        private Acumulador(Integer id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        private void registrarConcluida(OsFinalizadaProjecao os) {
            faturamento = faturamento.add(nuloComoZero(os.total()));
            concluidas++;
            if (os.dataInicio() != null && os.dataFinalizacao() != null) {
                somaDias += Math.max(0, ChronoUnit.DAYS.between(os.dataInicio(), os.dataFinalizacao()));
                comTempo++;
            }
        }

        private DesempenhoMecanico paraDesempenho(BigDecimal faturamentoPeriodo) {
            BigDecimal tempoMedio = comTempo == 0 ? null
                    : BigDecimal.valueOf(somaDias).divide(BigDecimal.valueOf(comTempo), 1, RoundingMode.HALF_UP);
            return new DesempenhoMecanico(
                    id,
                    nome,
                    dinheiro(faturamento),
                    concluidas,
                    emAndamento,
                    tempoMedio,
                    dividir(faturamento, concluidas),
                    percentual(faturamento, faturamentoPeriodo)
            );
        }
    }
}
