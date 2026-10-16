package com.cartoon.api.ordemServico.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemServicoRequest(
        @NotNull Integer servicoId,
        @NotNull @Positive Integer quantidade
) {
}
