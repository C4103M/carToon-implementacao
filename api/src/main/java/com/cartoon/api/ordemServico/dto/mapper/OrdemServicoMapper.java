package com.cartoon.api.ordemServico.dto.mapper;

import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.veiculo.Veiculo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class OrdemServicoMapper {

    private OrdemServicoMapper() {}

    public static OrdemServico paraOrdemServico(OrdemServicoRequest request,
                                                Veiculo veiculo, Oficina oficina, Usuario mecanico) {
        OrdemServico ordem = new OrdemServico();
        ordem.setDescricao(request.descricao());
        ordem.setVeiculo(veiculo);
        ordem.setOficina(oficina);
        ordem.setMecanico(mecanico);
        return ordem;
    }

    public static OrdemServicoResponse paraOrdemServicoResponse(OrdemServico ordem) {
        return new OrdemServicoResponse(
                ordem.getId(),
                ordem.getStatusServico(),
                ordem.getDescricao(),
                ordem.getVeiculo().getId(),
                ordem.getVeiculo().getPlaca(),
                ordem.getOficina().getId(),
                ordem.getMecanico().getId(),
                ordem.getMecanico().getNome(),
                ordem.getItensPeca().stream().map(OrdemServicoMapper::paraItemPecaResponse).toList(),
                ordem.getTotal());
    }

    public static ItemPecaResponse paraItemPecaResponse(ItemPeca item) {
        BigDecimal subtotal = item.getSubtotal();
        if (subtotal == null && item.getValorUnitario() != null && item.getQuantidade() != null) {
            subtotal = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        }
        return new ItemPecaResponse(
                item.getId(),
                item.getPeca().getId(),
                item.getPeca().getNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                subtotal);
    }

    public static ItemServicoResponse paraItemServicoResponse(ItemServico item) {
        Integer servicoId = item.getServico() != null ? item.getServico().getId() : null;
        String servicoNome = item.getServico() != null ? item.getServico().getNome() : null;
        Double subtotal = item.getSubtotal();
        if (subtotal == null && item.getValorUnitario() != null && item.getQuantidade() != null) {
            subtotal = item.getValorUnitario() * item.getQuantidade();
        }
        return new ItemServicoResponse(
                item.getId(),
                servicoId,
                servicoNome,
                item.getQuantidade(),
                item.getValorUnitario(),
                subtotal,
                item.getTempo()
        );
    }

    public static OrdemServicoHistoricoResponse paraOrdemServicoHistoricoResponse(OrdemServico ordem) {
        String mecanicoNome = ordem.getMecanico() != null ? ordem.getMecanico().getNome() : null;
        String oficinaNome = ordem.getOficina() != null ? ordem.getOficina().getNome() : null;

        List<ItemServicoResponse> servicos = ordem.getItensServico() != null ?
                ordem.getItensServico().stream().map(OrdemServicoMapper::paraItemServicoResponse).toList() : List.of();

        List<ItemPecaResponse> pecas = ordem.getItensPeca() != null ?
                ordem.getItensPeca().stream().map(OrdemServicoMapper::paraItemPecaResponse).toList() : List.of();

        BigDecimal total = ordem.getTotal();
        if (total == null) {
            BigDecimal totalPecas = pecas.stream()
                    .map(ItemPecaResponse::subtotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalServicos = servicos.stream()
                    .map(ItemServicoResponse::subtotal)
                    .filter(Objects::nonNull)
                    .map(BigDecimal::valueOf)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            total = totalPecas.add(totalServicos);
        }

        return new OrdemServicoHistoricoResponse(
                ordem.getId(),
                ordem.getDataOrcamento(),
                ordem.getDataInicio(),
                ordem.getDataFinalizacao(),
                ordem.getDataRejeicao(),
                ordem.getStatusServico(),
                ordem.getDescricao(),
                mecanicoNome,
                oficinaNome,
                servicos,
                pecas,
                total
        );
    }

    public static OrdemServicoResumo paraOrdemServicoResumo(OrdemServico ordem) {
        return new OrdemServicoResumo(
                ordem.getId(),
                ordem.getStatusServico(),
                ordem.getVeiculo().getPlaca(),
                ordem.getMecanico().getNome());
    }

    public static ItemPeca paraItemPeca(ItemPecaRequest request, Peca peca) {
        ItemPeca item = new ItemPeca();
        item.setPeca(peca);
        item.setQuantidade(request.quantidade());
        item.setValorUnitario(peca.getValorBase());
        item.calcSubtotal();
        return item;
    }
}