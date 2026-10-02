package com.cartoon.api.ordemServico.dto.response;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrdemServicoHistoricoResponse(
        Integer id,
        LocalDate dataOrcamento,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        LocalDate dataRejeicao,
        StatusServico status,
        String descricao,
        String mecanicoNome,
        String oficinaNome,
        List<ItemServicoResponse> itensServico,
        List<ItemPecaResponse> itensPeca,
        BigDecimal valorTotal
) {}
