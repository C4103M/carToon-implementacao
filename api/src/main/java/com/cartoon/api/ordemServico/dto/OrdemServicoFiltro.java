package com.cartoon.api.ordemServico.dto;

import com.cartoon.api.ordemServico.models.StatusServico;

public record OrdemServicoFiltro(
        Integer oficinaId,
        Integer veiculoId,
        String placa,
        Integer mecanicoId,
        StatusServico statusServico
) {
    public OrdemServicoFiltro(Integer oficinaId, Integer veiculoId, Integer mecanicoId, StatusServico statusServico) {
        this(oficinaId, veiculoId, null, mecanicoId, statusServico);
    }
}