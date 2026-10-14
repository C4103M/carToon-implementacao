package com.cartoon.api.ordemServico.dto.response;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrdemServicoResponse(
        Integer id,
        StatusServico status,
        LocalDate dataOrcamento,
        LocalDate dataInicio,
        LocalDate dataRejeicao,
        LocalDate dataFinalizacao,
        String descricao,
        Integer veiculoId,
        String placa,
        Integer oficinaId,
        Integer mecanicoId,
        String mecanicoNome,
        List<ItemPecaResponse> itensPeca,
        List<ItemServicoResponse> itensServico,
        BigDecimal total
) {
    public OrdemServicoResponse(
            Integer id,
            StatusServico status,
            LocalDate dataOrcamento,
            LocalDate dataInicio,
            LocalDate dataRejeicao,
            LocalDate dataFinalizacao,
            String descricao,
            Integer veiculoId,
            String placa,
            Integer oficinaId,
            Integer mecanicoId,
            String mecanicoNome,
            List<ItemPecaResponse> itensPeca,
            BigDecimal total
    ) {
        this(id, status, dataOrcamento, dataInicio, dataRejeicao, dataFinalizacao,
                descricao, veiculoId, placa, oficinaId, mecanicoId, mecanicoNome,
                itensPeca, List.of(), total);
    }
}
