package com.cartoon.api.dashboard.dto.projecao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OsFinalizadaProjecao(
        Integer id,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        BigDecimal total,
        Integer mecanicoId,
        String mecanicoNome
) {
}
