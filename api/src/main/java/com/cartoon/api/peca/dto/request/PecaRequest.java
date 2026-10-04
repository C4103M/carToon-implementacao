package com.cartoon.api.peca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PecaRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O fabricante é obrigatório")
        String fabricante,

        @NotNull(message = "O valor base é obrigatório")
        @Positive(message = "O valor base deve ser maior que zero")
        BigDecimal valorBase
) {
}
