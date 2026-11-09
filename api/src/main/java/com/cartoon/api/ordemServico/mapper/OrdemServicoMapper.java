package com.cartoon.api.ordemServico.mapper;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.ItemPecaResponse;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.Desconto;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.veiculo.model.Veiculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

public final class OrdemServicoMapper {

    private static final BigDecimal DIVISOR_FIPE = new BigDecimal("100000");

    private OrdemServicoMapper() {}

    public static OrdemServico paraOrdemServico(OrdemServicoRequest request,
                                                Veiculo veiculo, Oficina oficina, Usuario mecanico) {
        OrdemServico ordem = new OrdemServico();
        String desc = (request.descricao() != null && !request.descricao().isBlank())
                ? request.descricao().trim()
                : "Orçamento de manutenção";
        ordem.setDescricao(desc);
        ordem.setVeiculo(veiculo);
        ordem.setOficina(oficina);
        ordem.setMecanico(mecanico);
        ordem.setDesconto(request.desconto() != null ? request.desconto() : Desconto.SEM_DESCONTO);
        return ordem;
    }

    public static OrdemServicoResponse paraOrdemServicoResponse(OrdemServico ordem) {
        Veiculo veiculo = ordem.getVeiculo();
        Cliente cliente = (veiculo != null) ? veiculo.getCliente() : null;
        Oficina oficina = ordem.getOficina();
        Usuario mecanico = ordem.getMecanico();

        Integer veiculoId = veiculo != null ? veiculo.getId() : null;
        String veiculoPlaca = veiculo != null ? veiculo.getPlaca() : null;
        String veiculoMarca = veiculo != null ? veiculo.getMarca() : null;
        String veiculoModelo = veiculo != null ? veiculo.getModelo() : null;
        Integer veiculoAno = veiculo != null ? veiculo.getAnoModelo() : null;
        BigDecimal veiculoValorFipe = veiculo != null ? veiculo.getValorFipe() : null;

        Integer clienteId = cliente != null ? cliente.getId() : null;
        String clienteNome = cliente != null ? cliente.getNome() : null;
        String clienteCpf = cliente != null ? cliente.getCpf() : null;

        Integer oficinaId = oficina != null ? oficina.getId() : null;
        String oficinaNome = oficina != null ? oficina.getNome() : null;
        BigDecimal oficinaValorHora = oficina != null ? oficina.getValorHora() : null;
        BigDecimal oficinaTaxaRisco = oficina != null ? oficina.getTaxaRisco() : null;
        BigDecimal oficinaMargemPeca = oficina != null ? oficina.getMargemPeca() : null;

        Integer mecanicoId = mecanico != null ? mecanico.getId() : null;
        String mecanicoNome = mecanico != null ? mecanico.getNome() : null;

        List<ItemPecaResponse> itensPeca = ordem.getItensPeca() != null
                ? ordem.getItensPeca().stream().map(OrdemServicoMapper::paraItemPecaResponse).toList()
                : List.of();

        List<ItemServicoResponse> itensServico = ordem.getItensServico() != null
                ? ordem.getItensServico().stream().map(item -> paraItemServicoResponse(item, ordem)).toList()
                : List.of();

        BigDecimal totalPecasBruto = itensPeca.stream()
                .map(i -> i.subtotalBruto() != null ? i.subtotalBruto() : (i.subtotal() != null ? i.subtotal() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPecas = itensPeca.stream()
                .map(i -> i.subtotal() != null ? i.subtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalServicos = itensServico.stream()
                .map(i -> i.subtotal() != null ? i.subtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

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
                veiculoMarca,
                veiculoModelo,
                veiculoAno,
                veiculoValorFipe,
                clienteId,
                clienteNome,
                clienteCpf,
                oficinaId,
                oficinaNome,
                oficinaValorHora,
                oficinaTaxaRisco,
                oficinaMargemPeca,
                mecanicoId,
                mecanicoNome,
                itensPeca,
                itensServico,
                totalPecasBruto,
                totalPecas,
                totalServicos,
                ordem.getDesconto(),
                ordem.getValorBruto(),
                ordem.getValorDesconto(),
                ordem.getTotal()
        );
    }

    public static ItemPecaResponse paraItemPecaResponse(ItemPeca item) {
        BigDecimal subtotal = item.getSubtotal();
        if (subtotal == null && item.getValorUnitario() != null && item.getQuantidade() != null) {
            subtotal = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        }
        Integer pecaId = item.getPeca() != null ? item.getPeca().getId() : null;
        String pecaNome = item.getPeca() != null ? item.getPeca().getNome() : null;
        String fabricante = item.getPeca() != null ? item.getPeca().getFabricante() : null;

        BigDecimal valorBase = item.getValorBase();
        if (valorBase == null && item.getPeca() != null) {
            valorBase = item.getPeca().getValorBase();
        }
        if (valorBase == null) {
            valorBase = item.getValorUnitario();
        }

        BigDecimal margem = item.getMargem();
        if (margem == null) {
            if (item.getOrdemServico() != null && item.getOrdemServico().getOficina() != null && item.getOrdemServico().getOficina().getMargemPeca() != null) {
                margem = item.getOrdemServico().getOficina().getMargemPeca();
            } else {
                margem = BigDecimal.ONE;
            }
        }

        BigDecimal subtotalBruto = (valorBase != null && item.getQuantidade() != null)
                ? valorBase.multiply(BigDecimal.valueOf(item.getQuantidade())).setScale(2, RoundingMode.HALF_UP)
                : subtotal;

        return new ItemPecaResponse(
                item.getId(),
                pecaId,
                pecaNome,
                fabricante,
                item.getQuantidade(),
                valorBase,
                margem,
                item.getValorUnitario(),
                subtotalBruto,
                subtotal
        );
    }

    public static ItemServicoResponse paraItemServicoResponse(ItemServico item) {
        return paraItemServicoResponse(item, item.getOrdemServico());
    }

    public static ItemServicoResponse paraItemServicoResponse(ItemServico item, OrdemServico ordem) {
        Integer servicoId = item.getServico() != null ? item.getServico().getId() : null;
        String servicoNome = item.getServico() != null ? item.getServico().getNome() : null;
        BigDecimal subtotal = item.getSubtotal();
        if (subtotal == null && item.getValorUnitario() != null && item.getQuantidade() != null) {
            subtotal = item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        }

        LocalTime tempo = item.getTempo() != null
                ? item.getTempo()
                : (item.getServico() != null ? item.getServico().getTempo() : null);

        Double horas = item.getQuantidadeHoras() != null
                ? item.getQuantidadeHoras()
                : (item.getServico() != null ? item.getServico().getQuantidadeHoras() : null);

        BigDecimal peso = item.getPesoServico() != null
                ? item.getPesoServico()
                : (item.getServico() != null && item.getServico().getPesoServicoPadrao() != null ? item.getServico().getPesoServicoPadrao() : new BigDecimal("0.5"));

        BigDecimal valorHoraBase = null;
        BigDecimal multiplicadorFipe = null;

        if (ordem != null && ordem.getOficina() != null && ordem.getOficina().getValorHora() != null && horas != null) {
            valorHoraBase = BigDecimal.valueOf(horas)
                    .multiply(ordem.getOficina().getValorHora())
                    .setScale(2, RoundingMode.HALF_UP);

            if (ordem.getVeiculo() != null && ordem.getVeiculo().getValorFipe() != null) {
                BigDecimal taxa = ordem.getOficina().getTaxaRisco() != null
                        ? ordem.getOficina().getTaxaRisco()
                        : BigDecimal.ZERO;
                BigDecimal fatorFipe = ordem.getVeiculo().getValorFipe().divide(DIVISOR_FIPE, 8, RoundingMode.HALF_UP);
                BigDecimal adicionalRisco = fatorFipe.multiply(taxa).multiply(peso);
                multiplicadorFipe = BigDecimal.ONE.add(adicionalRisco).setScale(4, RoundingMode.HALF_UP);
            }
        }

        return new ItemServicoResponse(
                item.getId(),
                servicoId,
                servicoNome,
                item.getQuantidade(),
                item.getValorUnitario(),
                subtotal,
                horas,
                tempo,
                valorHoraBase,
                multiplicadorFipe,
                peso
        );
    }

    public static OrdemServicoResumo paraOrdemServicoResumo(OrdemServico ordem) {
        Veiculo veiculo = ordem.getVeiculo();
        Cliente cliente = veiculo != null ? veiculo.getCliente() : null;
        Integer veiculoId = veiculo != null ? veiculo.getId() : null;
        String placa = veiculo != null ? veiculo.getPlaca() : null;
        String veiculoMarca = veiculo != null ? veiculo.getMarca() : null;
        String veiculoModelo = veiculo != null ? veiculo.getModelo() : null;
        Integer clienteId = cliente != null ? cliente.getId() : null;
        String clienteNome = cliente != null ? cliente.getNome() : null;
        String mecanicoNome = ordem.getMecanico() != null ? ordem.getMecanico().getNome() : null;
        String oficinaNome = ordem.getOficina() != null ? ordem.getOficina().getNome() : null;

        List<ItemPecaResponse> itensPeca = (ordem.getItensPeca() == null || ordem.getItensPeca().isEmpty())
                ? List.of()
                : ordem.getItensPeca().stream().map(OrdemServicoMapper::paraItemPecaResponse).toList();

        BigDecimal totalPecasBruto = itensPeca.stream()
                .map(i -> i.subtotalBruto() != null ? i.subtotalBruto() : (i.subtotal() != null ? i.subtotal() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPecas = (ordem.getItensPeca() == null || ordem.getItensPeca().isEmpty())
                ? BigDecimal.ZERO
                : ordem.getItensPeca().stream()
                    .map(i -> {
                        if (i.getSubtotal() != null) return i.getSubtotal();
                        if (i.getValorUnitario() != null && i.getQuantidade() != null)
                            return i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()));
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalServicos = (ordem.getItensServico() == null || ordem.getItensServico().isEmpty())
                ? BigDecimal.ZERO
                : ordem.getItensServico().stream()
                    .map(i -> {
                        if (i.getSubtotal() != null) return i.getSubtotal();
                        if (i.getValorUnitario() != null && i.getQuantidade() != null)
                            return i.getValorUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()));
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

        return new OrdemServicoResumo(
                ordem.getId(),
                ordem.getDataOrcamento(),
                ordem.getDataInicio(),
                ordem.getDataFinalizacao(),
                ordem.getDataRejeicao(),
                ordem.getStatusServico(),
                ordem.getDescricao(),
                veiculoId,
                placa,
                veiculoMarca,
                veiculoModelo,
                clienteId,
                clienteNome,
                mecanicoNome,
                oficinaNome,
                totalPecasBruto,
                totalPecas,
                totalServicos,
                ordem.getValorBruto(),
                ordem.getDesconto(),
                ordem.getValorDesconto(),
                ordem.getTotal(),
                itensPeca
        );
    }

    public static ItemPeca paraItemPeca(ItemPecaRequest request, Peca peca, BigDecimal valorUnitario, BigDecimal margem) {
        ItemPeca item = new ItemPeca();
        item.setPeca(peca);
        item.setQuantidade(request.quantidade());
        item.setValorBase(peca != null ? peca.getValorBase() : null);
        item.setMargem(margem != null ? margem : BigDecimal.ONE);
        item.setValorUnitario(valorUnitario);
        item.calcSubtotal();
        return item;
    }

    public static ItemPeca paraItemPeca(ItemPecaRequest request, Peca peca, BigDecimal valorUnitario) {
        return paraItemPeca(request, peca, valorUnitario, BigDecimal.ONE);
    }

    public static ItemPeca paraItemPeca(ItemPecaRequest request, Peca peca) {
        return paraItemPeca(request, peca, peca != null ? peca.getValorBase() : null, BigDecimal.ONE);
    }

    public static ItemServico paraItemServico(ItemServicoRequest request, Servico servico) {
        ItemServico item = new ItemServico();
        item.setServico(servico);
        item.setQuantidade(request.quantidade());
        item.setTempo(servico != null ? servico.getTempo() : null);
        BigDecimal peso = request.pesoServico() != null
                ? request.pesoServico()
                : (servico != null && servico.getPesoServicoPadrao() != null ? servico.getPesoServicoPadrao() : new BigDecimal("0.5"));
        item.setPesoServico(peso);
        item.calcSubtotal();
        return item;
    }
}