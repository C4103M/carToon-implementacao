package com.cartoon.api.ordemServico.dto.response;

import java.time.LocalTime;

public record ItemServicoResponse(
        Integer id,
        Integer servicoId,
        String servicoNome,
        Integer quantidade,
        Double valorUnitario,
        Double subtotal,
        LocalTime tempo
) {}
