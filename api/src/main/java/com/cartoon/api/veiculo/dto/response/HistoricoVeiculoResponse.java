package com.cartoon.api.veiculo.dto.response;

import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;

import java.util.List;

public record HistoricoVeiculoResponse(
        VeiculoResumo veiculo,
        List<OrdemServicoResumo> ordensServico,
        int totalOrdens
) {}
