package com.cartoon.api.servico;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ServicoResponse(
        Integer id,
        String nome,
        BigDecimal valorBase,
        String descricao,
        LocalTime tempoEstimado
) {
    public static ServicoResponse daEntidade(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getValorBase(),
                servico.getDescricao(),
                servico.getTempoEstimado()
        );
    }
}
