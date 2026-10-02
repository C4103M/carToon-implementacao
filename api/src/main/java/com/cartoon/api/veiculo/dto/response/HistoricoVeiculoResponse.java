package com.cartoon.api.veiculo.dto.response;

import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HistoricoVeiculoResponse(
        VeiculoResumo veiculo,
        String mensagem,
        List<OrdemServicoResumo> ordensServico,
        int totalOrdens
) {}
