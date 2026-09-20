package com.cartoon.api.ordemServico.dto.response;

import com.cartoon.api.ordemServico.models.ItemPeca;

import java.math.BigDecimal;

public record ItemPecaResponse(Integer id, Integer pecaId, String pecaNome, Integer quantidade, BigDecimal valorUnitario, BigDecimal subtotal) {

}