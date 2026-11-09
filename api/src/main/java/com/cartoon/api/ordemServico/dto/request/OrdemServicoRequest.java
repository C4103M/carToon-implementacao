package com.cartoon.api.ordemServico.dto.request;

import com.cartoon.api.ordemServico.models.Desconto;
import jakarta.validation.constraints.NotNull;

public record OrdemServicoRequest(
        @NotNull(message = "O veículo é obrigatório")
        Integer veiculoId,
        @NotNull(message = "O id mecânico responsável é obrigatório")
        Integer mecanicoId,
        String descricao,
        Desconto desconto
) {
    public OrdemServicoRequest(Integer veiculoId, Integer mecanicoId, String descricao) {
        this(veiculoId, mecanicoId, descricao, Desconto.SEM_DESCONTO);
    }
}
