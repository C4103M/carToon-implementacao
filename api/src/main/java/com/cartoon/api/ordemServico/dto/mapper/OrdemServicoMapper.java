package com.cartoon.api.ordemServico.dto.mapper;

import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.usuario.Usuario;
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
        return new ItemPecaResponse(
                item.getId(),
                item.getPeca().getId(),
                item.getPeca().getNome(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
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