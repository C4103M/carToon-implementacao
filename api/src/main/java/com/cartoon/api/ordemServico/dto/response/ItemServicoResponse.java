package com.cartoon.api.ordemServico.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ItemServicoResponse(
        Integer id,
        Integer servicoId,
        String servicoNome,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal,
        Double quantidadeHoras,
        @JsonFormat(pattern = "HH:mm")
        LocalTime tempo,
        BigDecimal valorHoraBase,
        BigDecimal multiplicadorFipe,
        BigDecimal pesoServico
) {
    public ItemServicoResponse(
            Integer id,
            Integer servicoId,
            String servicoNome,
            Integer quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal,
            Double quantidadeHoras
    ) {
        this(id, servicoId, servicoNome, quantidade, valorUnitario, subtotal, quantidadeHoras, null, null, null, new BigDecimal("0.5"));
    }

    public ItemServicoResponse(
            Integer id,
            Integer servicoId,
            String servicoNome,
            Integer quantidade,
            BigDecimal valorUnitario,
            BigDecimal subtotal,
            Double quantidadeHoras,
            LocalTime tempo,
            BigDecimal valorHoraBase,
            BigDecimal multiplicadorFipe
    ) {
        this(id, servicoId, servicoNome, quantidade, valorUnitario, subtotal, quantidadeHoras, tempo, valorHoraBase, multiplicadorFipe, new BigDecimal("0.5"));
    }
}
