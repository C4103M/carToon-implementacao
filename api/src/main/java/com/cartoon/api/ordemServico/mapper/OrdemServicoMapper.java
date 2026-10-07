package com.cartoon.api.ordemServico.mapper;

import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.veiculo.Veiculo;

import java.math.BigDecimal;

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
        String veiculoPlaca = ordem.getVeiculo() != null ? ordem.getVeiculo().getPlaca() : null;
        Integer veiculoId = ordem.getVeiculo() != null ? ordem.getVeiculo().getId() : null;
        Integer oficinaId = ordem.getOficina() != null ? ordem.getOficina().getId() : null;
        Integer mecanicoId = ordem.getMecanico() != null ? ordem.getMecanico().getId() : null;
        String mecanicoNome = ordem.getMecanico() != null ? ordem.getMecanico().getNome() : null;

        return new OrdemServicoResponse(
                ordem.getId(),
                ordem.getStatusServico(),
                ordem.getDataOrcamento(),
                ordem.getDataInicio(),
                ordem.getDataRejeicao(),
                ordem.getDataFinalizacao(),
                ordem.getDescricao(),
                veiculoId,
                veiculoPlaca,
                oficinaId,
                mecanicoId,
                mecanicoNome,
                ordem.getItensPeca() != null ? ordem.getItensPeca().stream().map(OrdemServicoMapper::paraItemPecaResponse).toList() : java.util.List.of(),
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

    public static OrdemServicoResumo paraOrdemServicoResumo(OrdemServico ordem) {
        String placa = ordem.getVeiculo() != null ? ordem.getVeiculo().getPlaca() : null;
        String mecanicoNome = ordem.getMecanico() != null ? ordem.getMecanico().getNome() : null;
        String oficinaNome = ordem.getOficina() != null ? ordem.getOficina().getNome() : null;

        return new OrdemServicoResumo(
                ordem.getId(),
                ordem.getDataOrcamento(),
                ordem.getDataInicio(),
                ordem.getDataFinalizacao(),
                ordem.getDataRejeicao(),
                ordem.getStatusServico(),
                ordem.getDescricao(),
                placa,
                mecanicoNome,
                oficinaNome,
                ordem.getTotal()
        );
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