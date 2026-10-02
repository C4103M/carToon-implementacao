package com.cartoon.api.ordemServico.dto;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrdemServicoResumo(
        Integer id,
        LocalDate dataOrcamento,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        LocalDate dataRejeicao,
        StatusServico statusServico,
        String descricao,
        String placa,
        String mecanicoNome,
        String oficinaNome,
        BigDecimal valorTotal
) {
    public OrdemServicoResumo(Integer id, StatusServico statusServico, String placa, String mecanicoNome) {
        this(id, null, null, null, null, statusServico, null, placa, mecanicoNome, null, null);
    }
}