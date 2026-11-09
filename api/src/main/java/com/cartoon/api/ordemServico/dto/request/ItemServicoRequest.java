package com.cartoon.api.ordemServico.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ItemServicoRequest(
        @NotNull Integer servicoId,
        @NotNull @Positive Integer quantidade,
        @DecimalMin(value = "0.0", inclusive = true, message = "O peso do serviço deve ser no mínimo 0.0")
        @DecimalMax(value = "1.0", inclusive = true, message = "O peso do serviço deve ser no máximo 1.0")
        @JsonAlias({"peso", "peso_servico"})
        BigDecimal pesoServico
) {
    public ItemServicoRequest(Integer servicoId, Integer quantidade) {
        this(servicoId, quantidade, null);
    }
}
