package com.cartoon.api.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalTime;

public record ServicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,
        
        @NotNull(message = "O valor base é obrigatório")
        @Positive(message = "O valor base deve ser maior que zero")
        BigDecimal valorBase,
        
        String descricao,
        
        LocalTime tempoEstimado
) {
}
