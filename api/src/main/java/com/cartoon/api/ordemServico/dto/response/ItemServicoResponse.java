package com.cartoon.api.ordemServico.dto.response;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ItemServicoResponse(
        Integer id,
        Integer servicoId,
        String servicoNome,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal,
        LocalTime tempo
) {}
