package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.DashboardAdminResponse.Faturamento;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.PontoValor;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.ReceitaPorTipo;
import com.cartoon.api.dashboard.dto.projecao.OsFinalizadaProjecao;
import com.cartoon.api.dashboard.dto.projecao.SomaPorOsProjecao;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.cartoon.api.compartilhado.util.BigDecimalUtils.dinheiro;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.dividir;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.nuloComoZero;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.variacaoPercentual;
import static com.cartoon.api.compartilhado.util.BigDecimalUtils.zeroMonetario;

@Component
public class CalculadoraFaturamento {

    public BigDecimal somar(List<OsFinalizadaProjecao> ordens) {
        return dinheiro(ordens.stream()
                .map(os -> nuloComoZero(os.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public Faturamento resumo(BigDecimal totalAcumulado, List<OsFinalizadaProjecao> noPeriodo,
                              List<OsFinalizadaProjecao> noAnterior) {
        BigDecimal periodo = somar(noPeriodo);
        BigDecimal anterior = somar(noAnterior);
        return new Faturamento(
                dinheiro(totalAcumulado),
                periodo,
                anterior,
                variacaoPercentual(periodo, anterior),
                dividir(periodo, noPeriodo.size()),
                noPeriodo.size()
        );
    }

    public List<PontoValor> mensal(List<OsFinalizadaProjecao> finalizadas, PeriodoDashboard periodo) {
        Map<YearMonth, BigDecimal> porMes = new LinkedHashMap<>();
        for (YearMonth m = periodo.primeiroMes(); !m.isAfter(periodo.ultimoMes()); m = m.plusMonths(1)) {
            porMes.put(m, BigDecimal.ZERO);
        }
        for (OsFinalizadaProjecao os : finalizadas) {
            LocalDate data = os.dataFinalizacao();
            if (data == null || data.isAfter(periodo.fim())) {
                continue;
            }
            porMes.computeIfPresent(YearMonth.from(data), (k, v) -> v.add(nuloComoZero(os.total())));
        }
        List<PontoValor> pontos = new ArrayList<>();
        porMes.forEach((mes, valor) -> pontos.add(new PontoValor(mes.toString(), dinheiro(valor))));
        return pontos;
    }

    public List<PontoValor> evolucao(List<OsFinalizadaProjecao> noPeriodo, PeriodoDashboard periodo) {
        Map<String, BigDecimal> serie = new LinkedHashMap<>();
        SerieTemporal.chaves(periodo.inicio(), periodo.fim(), periodo.granularidade())
                .forEach(c -> serie.put(c, BigDecimal.ZERO));
        for (OsFinalizadaProjecao os : noPeriodo) {
            serie.computeIfPresent(SerieTemporal.chave(os.dataFinalizacao(), periodo.granularidade()),
                    (k, v) -> v.add(nuloComoZero(os.total())));
        }
        List<PontoValor> pontos = new ArrayList<>();
        serie.forEach((k, v) -> pontos.add(new PontoValor(k, dinheiro(v))));
        return pontos;
    }

    public ReceitaPorTipo receitaPorTipo(List<OsFinalizadaProjecao> noPeriodo,
                                         List<SomaPorOsProjecao> pecas,
                                         List<SomaPorOsProjecao> servicos) {
        if (noPeriodo.isEmpty()) {
            return new ReceitaPorTipo(zeroMonetario(), zeroMonetario());
        }
        Map<Integer, BigDecimal> pecasPorOs = porOrdem(pecas);
        Map<Integer, BigDecimal> servicosPorOs = porOrdem(servicos);

        BigDecimal totalPecas = BigDecimal.ZERO;
        BigDecimal totalServicos = BigDecimal.ZERO;
        for (OsFinalizadaProjecao os : noPeriodo) {
            BigDecimal liquido = nuloComoZero(os.total());
            BigDecimal brutoPecas = pecasPorOs.getOrDefault(os.id(), BigDecimal.ZERO);
            BigDecimal bruto = brutoPecas.add(servicosPorOs.getOrDefault(os.id(), BigDecimal.ZERO));
            if (bruto.signum() == 0) {
                totalServicos = totalServicos.add(liquido);
                continue;
            }
            BigDecimal pecasLiquido = liquido.multiply(brutoPecas).divide(bruto, 2, RoundingMode.HALF_UP);
            totalPecas = totalPecas.add(pecasLiquido);
            totalServicos = totalServicos.add(liquido.subtract(pecasLiquido));
        }
        return new ReceitaPorTipo(dinheiro(totalServicos), dinheiro(totalPecas));
    }

    private static Map<Integer, BigDecimal> porOrdem(List<SomaPorOsProjecao> somas) {
        Map<Integer, BigDecimal> mapa = new HashMap<>();
        for (SomaPorOsProjecao s : somas) {
            mapa.merge(s.ordemServicoId(), nuloComoZero(s.valor()), BigDecimal::add);
        }
        return mapa;
    }
}
