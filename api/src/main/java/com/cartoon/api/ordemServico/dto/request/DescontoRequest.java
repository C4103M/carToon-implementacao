package com.cartoon.api.ordemServico.dto.request;

import com.cartoon.api.ordemServico.models.Desconto;
import jakarta.validation.constraints.NotNull;

public record DescontoRequest(
        @NotNull(message = "O desconto é obrigatório")
        Desconto desconto
) {
}
