package com.cartoon.api.servico.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ServicoResponse(
        Integer id,
        String nome,
        BigDecimal valorBase,
        String descricao,
        LocalTime tempoEstimado
) {
}
