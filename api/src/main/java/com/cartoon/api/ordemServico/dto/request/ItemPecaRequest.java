package com.cartoon.api.ordemServico.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPecaRequest(@NotNull Integer pecaId, @NotNull @Positive Integer quantidade) {

}
