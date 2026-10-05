package com.cartoon.api.ordemServico.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrdemServicoRequest(
         @NotNull Integer veiculoId,
         @NotNull Integer oficinaId,
         @NotNull Integer mecanicoId,
         String descricao
) {
}
