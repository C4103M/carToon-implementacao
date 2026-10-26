package com.cartoon.api.peca.dto.response;

import java.math.BigDecimal;

public record PecaResponse(
        Integer id,
        String nome,
        String fabricante,
        BigDecimal valorBase,
        Integer oficinaId,
        Boolean ativo
) {
    public PecaResponse(Integer id, String nome, String fabricante, BigDecimal valorBase, Integer oficinaId) {
        this(id, nome, fabricante, valorBase, oficinaId, true);
    }

    public PecaResponse(Integer id, String nome, String fabricante, BigDecimal valorBase) {
        this(id, nome, fabricante, valorBase, null, true);
    }
}
