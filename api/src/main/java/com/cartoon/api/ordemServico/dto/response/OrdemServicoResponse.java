package com.cartoon.api.ordemServico.dto.response;

import com.cartoon.api.ordemServico.models.Desconto;
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
        String veiculoMarca,
        String veiculoModelo,
        Integer veiculoAno,
        BigDecimal veiculoValorFipe,
        Integer clienteId,
        String clienteNome,
        String clienteCpf,
        Integer oficinaId,
        String oficinaNome,
        BigDecimal oficinaValorHora,
        BigDecimal oficinaTaxaRisco,
        Integer mecanicoId,
        String mecanicoNome,
        List<ItemPecaResponse> itensPeca,
        List<ItemServicoResponse> itensServico,
        BigDecimal totalPecas,
        BigDecimal totalServicos,
        Desconto desconto,
        BigDecimal valorBruto,
        BigDecimal valorDesconto,
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
            List<ItemServicoResponse> itensServico,
            Desconto desconto,
            BigDecimal valorBruto,
            BigDecimal valorDesconto,
            BigDecimal total
    ) {
        this(
                id, status, dataOrcamento, dataInicio, dataRejeicao, dataFinalizacao,
                descricao, veiculoId, placa, null, null, null, null,
                null, null, null,
                oficinaId, null, null, null,
                mecanicoId, mecanicoNome,
                itensPeca, itensServico,
                BigDecimal.ZERO, BigDecimal.ZERO,
                desconto, valorBruto, valorDesconto, total
        );
    }

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
            List<ItemServicoResponse> itensServico,
            BigDecimal total
    ) {
        this(
                id, status, dataOrcamento, dataInicio, dataRejeicao, dataFinalizacao,
                descricao, veiculoId, placa, oficinaId, mecanicoId, mecanicoNome,
                itensPeca, itensServico, Desconto.SEM_DESCONTO, total, BigDecimal.ZERO, total
        );
    }

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
        this(
                id, status, dataOrcamento, dataInicio, dataRejeicao, dataFinalizacao,
                descricao, veiculoId, placa, oficinaId, mecanicoId, mecanicoNome,
                itensPeca, List.of(), Desconto.SEM_DESCONTO, total, BigDecimal.ZERO, total
        );
    }
}
