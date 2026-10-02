package com.cartoon.api.veiculo.dto.mapper;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.veiculo.Veiculo;
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
        veiculo.setModelo(request.modelo().trim());
        veiculo.setMontadora(request.montadora().trim());
        veiculo.setAno(request.ano());
        veiculo.setValorFipe(null); // Valor FIPE permanece null por enquanto até integração com API externa
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
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getMontadora(),
                veiculo.getValorFipe(),
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
                veiculo.getModelo(),
                veiculo.getMontadora(),
                veiculo.getAno(),
                veiculo.getValorFipe(),
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
        if (request.montadora() != null && !request.montadora().isBlank()) {
            veiculo.setMontadora(request.montadora().trim());
        }
        if (request.modelo() != null && !request.modelo().isBlank()) {
            veiculo.setModelo(request.modelo().trim());
        }
        if (request.ano() != null) {
            veiculo.setAno(request.ano());
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
