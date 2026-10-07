package com.cartoon.api.veiculo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record VeiculoAtualizacaoRequest(
        String montadora,
        String modelo,

        @Min(value = 1900, message = "Ano inválido")
        Integer ano,

        @Pattern(
                regexp = "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$",
                message = "Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul."
        )
        String placa,

        Integer clienteId,
        Boolean ativo
) {}
