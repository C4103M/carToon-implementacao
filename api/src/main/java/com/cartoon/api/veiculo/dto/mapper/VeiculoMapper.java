package com.cartoon.api.veiculo.dto.mapper;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResumo;

import java.util.List;

public final class VeiculoMapper {

    private VeiculoMapper() {}

    public static Veiculo paraVeiculo(VeiculoRequest request, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(request.placa());
        veiculo.setMarca(request.marca().trim());
        veiculo.setModelo(request.modelo().trim());
        veiculo.setAnoModelo(request.anoModelo());
        veiculo.setCombustivel(request.combustivel() != null ? request.combustivel().trim() : null);
        veiculo.setCodigoFipe(request.codigoFipe() != null && !request.codigoFipe().isBlank() ? request.codigoFipe().trim() : null);
        veiculo.setValorFipe(request.valorFipe());
        veiculo.setMesReferencia(request.mesReferencia() != null && !request.mesReferencia().isBlank() ? request.mesReferencia().trim() : null);
        veiculo.setAtivo(true);
        veiculo.setCliente(cliente);
        return veiculo;
    }

    public static VeiculoResponse paraVeiculoResponse(Veiculo veiculo) {
        Integer clienteId = veiculo.getCliente() != null ? veiculo.getCliente().getId() : null;
        String clienteNome = veiculo.getCliente() != null ? veiculo.getCliente().getNome() : null;
        String clienteCpf = veiculo.getCliente() != null ? veiculo.getCliente().getCpf() : null;

        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAnoModelo(),
                veiculo.getCombustivel(),
                veiculo.getCodigoFipe(),
                veiculo.getValorFipe(),
                veiculo.getMesReferencia(),
                veiculo.getAtivo(),
                clienteId,
                clienteNome,
                clienteCpf
        );
    }

    public static VeiculoResumo paraVeiculoResumo(Veiculo veiculo) {
        String clienteNome = veiculo.getCliente() != null ? veiculo.getCliente().getNome() : null;

        return new VeiculoResumo(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAnoModelo(),
                veiculo.getCombustivel(),
                veiculo.getCodigoFipe(),
                veiculo.getValorFipe(),
                veiculo.getMesReferencia(),
                veiculo.getAtivo(),
                clienteNome
        );
    }

    public static HistoricoVeiculoResponse paraHistoricoVeiculoResponse(Veiculo veiculo, List<OrdemServicoResumo> ordens) {
        VeiculoResumo veiculoResumo = paraVeiculoResumo(veiculo);
        List<OrdemServicoResumo> lista = ordens != null ? ordens : List.of();
        return new HistoricoVeiculoResponse(
                veiculoResumo,
                lista,
                lista.size()
        );
    }

    public static void atualizarDados(Veiculo veiculo, VeiculoAtualizacaoRequest request, Cliente novoCliente) {
        if (request.marca() != null && !request.marca().isBlank()) {
            veiculo.setMarca(request.marca().trim());
        }
        if (request.modelo() != null && !request.modelo().isBlank()) {
            veiculo.setModelo(request.modelo().trim());
        }
        if (request.anoModelo() != null) {
            veiculo.setAnoModelo(request.anoModelo());
        }
        if (request.combustivel() != null && !request.combustivel().isBlank()) {
            veiculo.setCombustivel(request.combustivel().trim());
        }
        if (request.codigoFipe() != null) {
            veiculo.setCodigoFipe(request.codigoFipe().isBlank() ? null : request.codigoFipe().trim());
        }
        if (request.valorFipe() != null) {
            veiculo.setValorFipe(request.valorFipe());
        }
        if (request.mesReferencia() != null) {
            veiculo.setMesReferencia(request.mesReferencia().isBlank() ? null : request.mesReferencia().trim());
        }
        if (request.placa() != null && !request.placa().isBlank()) {
            veiculo.setPlaca(request.placa());
        }
        if (novoCliente != null) {
            veiculo.setCliente(novoCliente);
        }
        if (request.ativo() != null) {
            veiculo.setAtivo(request.ativo());
        }
    }
}
