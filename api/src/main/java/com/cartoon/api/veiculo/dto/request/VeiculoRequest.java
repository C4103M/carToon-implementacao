package com.cartoon.api.veiculo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record VeiculoRequest(
        @NotNull(message = "O ID do cliente é obrigatório")
        Integer clienteId,

        @NotBlank(message = "A placa é obrigatória")
        @Pattern(
                regexp = "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$",
                message = "Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul."
        )
        String placa,

        @NotBlank(message = "A marca é obrigatória")
        String marca,

        @NotBlank(message = "O modelo é obrigatório")
        String modelo,

        @NotNull(message = "O ano do modelo é obrigatório")
        @Min(value = 1900, message = "Ano inválido")
        Integer anoModelo,

        @NotBlank(message = "O combustível é obrigatório")
        String combustivel,

        String codigoFipe,

        BigDecimal valorFipe,

        String mesReferencia
) {}
