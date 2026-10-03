package com.cartoon.api.peca;

import java.math.BigDecimal;

public record PecaResponse(
        Integer id,
        String nome,
        String fabricante,
        BigDecimal valorBase
) {
    public static PecaResponse daEntidade(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getFabricante(),
                peca.getValorBase()
        );
    }
}
