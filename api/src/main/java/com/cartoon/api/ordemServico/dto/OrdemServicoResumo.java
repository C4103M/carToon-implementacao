package com.cartoon.api.ordemServico.dto;

import com.cartoon.api.ordemServico.models.StatusServico;

public record OrdemServicoResumo(
        Integer id,
        StatusServico statusServico,
        String placa,
        String mecanicoNome) {}