package com.cartoon.api.ordemServico.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ItemPecaResponse(
        Integer id,
        Integer pecaId,
        String pecaNome,
        String fabricante,
        Integer quantidade,
        BigDecimal valorBase,
        BigDecimal margem,
        BigDecimal valorUnitario,
        BigDecimal subtotalBruto,
        BigDecimal subtotal
) {
    public ItemPecaResponse(
            Integer id,
            Integer pecaId,
            String pecaNome,
            String fabricante,
            Integer quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal
    ) {
        this(id, pecaId, pecaNome, fabricante, quantidade, valorUnitario, BigDecimal.ONE, valorUnitario, subtotal, subtotal);
    }

    public ItemPecaResponse(
            Integer id,
            Integer pecaId,
            String pecaNome,
            Integer quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal
    ) {
        this(id, pecaId, pecaNome, null, quantidade, valorUnitario, subtotal);
    }

    @JsonProperty("nomePeca")
    public String nomePeca() {
        return pecaNome;
    }

    @JsonProperty("valorBruto")
    public BigDecimal valorBruto() {
        return valorBase;
    }

    @JsonProperty("valorFinal")
    public BigDecimal valorFinal() {
        return valorUnitario;
    }
}