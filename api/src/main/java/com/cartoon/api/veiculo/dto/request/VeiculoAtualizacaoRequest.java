package com.cartoon.api.veiculo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record VeiculoAtualizacaoRequest(
        String marca,
        String modelo,

        @Min(value = 1900, message = "Ano inválido")
        Integer anoModelo,

        String combustivel,
        String codigoFipe,

        @Positive(message = "O valor FIPE deve ser maior que zero")
        BigDecimal valorFipe,

        String mesReferencia,

        @Pattern(
                regexp = "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$",
                message = "Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul."
        )
        String placa,

        Integer clienteId,
        Boolean ativo
) {}
