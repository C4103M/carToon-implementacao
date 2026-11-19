package com.cartoon.api.ordemServico.dto;

import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.models.Desconto;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrdemServicoResumo(
        Integer id,
        LocalDate dataOrcamento,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        LocalDate dataRejeicao,
        StatusServico statusServico,
        String descricao,
        Integer veiculoId,
        String placa,
        String veiculoMarca,
        String veiculoModelo,
        Integer clienteId,
        String clienteNome,
        String mecanicoNome,
        String oficinaNome,
        BigDecimal totalPecasBruto,
        BigDecimal totalPecas,
        BigDecimal totalServicos,
        BigDecimal valorBruto,
        Desconto desconto,
        BigDecimal valorDesconto,
        BigDecimal valorTotal,
        List<ItemPecaResponse> itensPeca
) {
    public OrdemServicoResumo(
            Integer id,
            LocalDate dataOrcamento,
            LocalDate dataInicio,
            LocalDate dataFinalizacao,
            LocalDate dataRejeicao,
            StatusServico statusServico,
            String descricao,
            Integer veiculoId,
            String placa,
            String veiculoMarca,
            String veiculoModelo,
            Integer clienteId,
            String clienteNome,
            String mecanicoNome,
            String oficinaNome,
            BigDecimal totalPecas,
            BigDecimal totalServicos,
            BigDecimal valorBruto,
            Desconto desconto,
            BigDecimal valorDesconto,
            BigDecimal valorTotal
    ) {
        this(
                id, dataOrcamento, dataInicio, dataFinalizacao, dataRejeicao,
                statusServico, descricao, veiculoId, placa, veiculoMarca, veiculoModelo,
                clienteId, clienteNome, mecanicoNome, oficinaNome,
                totalPecas, totalPecas, totalServicos,
                valorBruto, desconto, valorDesconto, valorTotal,
                List.of()
        );
    }

    public OrdemServicoResumo(
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
        this(
                id, dataOrcamento, dataInicio, dataFinalizacao, dataRejeicao,
                statusServico, descricao, null, placa, null, null,
                null, null, mecanicoNome, oficinaNome,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, valorTotal,
                Desconto.SEM_DESCONTO, BigDecimal.ZERO, valorTotal,
                List.of()
        );
    }

    public OrdemServicoResumo(Integer id, StatusServico statusServico, String placa, String mecanicoNome) {
        this(id, null, null, null, null, statusServico, null, placa, mecanicoNome, null, null);
    }

    @JsonProperty("status")
    public StatusServico status() {
        return statusServico;
    }

    @JsonProperty("total")
    public BigDecimal total() {
        return valorTotal;
    }
}