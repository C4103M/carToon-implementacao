package com.cartoon.api.dashboard.service;

import com.cartoon.api.dashboard.dto.DashboardAdminResponse.PontoQuantidade;
import com.cartoon.api.dashboard.dto.DashboardAdminResponse.QuantidadePorStatus;
import com.cartoon.api.dashboard.dto.projecao.OsAberturaProjecao;
import com.cartoon.api.ordemServico.models.StatusServico;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CalculadoraOrdens {

    public List<PontoQuantidade> volume(List<OsAberturaProjecao> abertas, StatusServico status,
                                        PeriodoDashboard periodo) {
        Map<String, Long> serie = new LinkedHashMap<>();
        SerieTemporal.chaves(periodo.inicio(), periodo.fim(), periodo.granularidade())
                .forEach(c -> serie.put(c, 0L));
        for (OsAberturaProjecao os : abertas) {
            if (os.dataOrcamento() == null || (status != null && status != os.status())) {
                continue;
            }
            serie.computeIfPresent(SerieTemporal.chave(os.dataOrcamento(), periodo.granularidade()), (k, v) -> v + 1);
        }
        List<PontoQuantidade> pontos = new ArrayList<>();
        serie.forEach((k, v) -> pontos.add(new PontoQuantidade(k, v)));
        return pontos;
    }

    public List<QuantidadePorStatus> porStatus(List<OsAberturaProjecao> abertas) {
        Map<StatusServico, Long> contagem = new EnumMap<>(StatusServico.class);
        for (StatusServico s : StatusServico.values()) {
            contagem.put(s, 0L);
        }
        for (OsAberturaProjecao os : abertas) {
            if (os.status() != null) {
                contagem.merge(os.status(), 1L, Long::sum);
            }
        }
        List<QuantidadePorStatus> lista = new ArrayList<>();
        contagem.forEach((s, q) -> lista.add(new QuantidadePorStatus(s, q)));
        return lista;
    }
}
