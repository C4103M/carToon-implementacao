package com.cartoon.api.ordemServico.dto;

import com.cartoon.api.ordemServico.models.StatusServico;

public record OrdemServicoFiltro(
        Integer oficinaId,
        Integer veiculoId,
        String placa,
        Integer mecanicoId,
        StatusServico statusServico,
        StatusServico status
) {
    public OrdemServicoFiltro(
            Integer oficinaId,
            Integer veiculoId,
            String placa,
            Integer mecanicoId,
            StatusServico statusServico
    ) {
        this(oficinaId, veiculoId, placa, mecanicoId, statusServico, null);
    }

    public OrdemServicoFiltro(
            Integer oficinaId,
            Integer veiculoId,
            Integer mecanicoId,
            StatusServico statusServico
    ) {
        this(oficinaId, veiculoId, null, mecanicoId, statusServico, null);
    }

    public StatusServico statusEfetivo() {
        return statusServico != null ? statusServico : status;
    }
}
