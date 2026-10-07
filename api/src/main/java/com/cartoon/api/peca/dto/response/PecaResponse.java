package com.cartoon.api.peca.dto.response;

import java.math.BigDecimal;

public record PecaResponse(
        Integer id,
        String nome,
        String fabricante,
        BigDecimal valorBase
) {
}
