package com.cartoon.api.veiculo;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.PlacaInvalidaException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.mapper.OrdemServicoMapper;
import com.cartoon.api.veiculo.dto.mapper.VeiculoMapper;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.specs.VeiculoSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private static final Pattern PADRAO_PLACA = Pattern.compile(
            "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$"
    );

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;

    @Transactional
    public VeiculoResponse cadastrar(VeiculoRequest request) {
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        if (!cliente.isAtivo()) {
            throw new ClienteInativoException();
        }

        if (request.placa() == null || !PADRAO_PLACA.matcher(request.placa().trim()).matches()) {
            throw new PlacaInvalidaException();
        }

        String placaNormalizada = normalizarPlaca(request.placa());
        if (veiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema.");
        }

        Veiculo veiculo = VeiculoMapper.paraVeiculo(request, cliente);
        veiculo.setPlaca(placaNormalizada);
        veiculo = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse atualizar(Integer id, VeiculoAtualizacaoRequest request) {
        Veiculo veiculo = buscarEntidade(id);

        Cliente novoCliente = null;
        if (request.clienteId() != null && !request.clienteId().equals(veiculo.getCliente().getId())) {
            novoCliente = clienteService.buscarEntidade(request.clienteId());
            if (!novoCliente.isAtivo()) {
                throw new ClienteInativoException();
            }
        }

        if (request.placa() != null && !request.placa().isBlank()) {
            if (!PADRAO_PLACA.matcher(request.placa().trim()).matches()) {
                throw new PlacaInvalidaException();
            }
            String placaNormalizada = normalizarPlaca(request.placa());
            if (veiculoRepository.existsByPlacaIgnoreCaseAndIdNot(placaNormalizada, id)) {
                throw new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema.");
            }
        }

        VeiculoMapper.atualizarDados(veiculo, request, novoCliente);
        veiculo = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Integer id) {
        Veiculo veiculo = veiculoRepository.findComClienteById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorPlaca(String placa) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public Page<VeiculoResponse> listar(VeiculoFiltro filtro, Pageable pageable) {
        Specification<Veiculo> spec = VeiculoSpecs.montarFiltros(filtro);
        return veiculoRepository.findAll(spec, pageable).map(VeiculoMapper::paraVeiculoResponse);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarPorCliente(Integer clienteId) {
        clienteService.buscarEntidade(clienteId);
        return veiculoRepository.findByClienteId(clienteId).stream()
                .map(VeiculoMapper::paraVeiculoResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse inativar(Integer id) {
        Veiculo veiculo = buscarEntidade(id);
        veiculo.setAtivo(false);
        veiculo = veiculoRepository.save(veiculo);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse reativar(Integer id) {
        Veiculo veiculo = buscarEntidade(id);
        veiculo.setAtivo(true);
        veiculo = veiculoRepository.save(veiculo);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public HistoricoVeiculoResponse consultarHistorico(String placa) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        List<OrdemServicoResumo> ordens = veiculo.getOrdensServicos() != null
                ? veiculo.getOrdensServicos().stream()
                        .sorted((a, b) -> {
                            if (a.getDataOrcamento() == null && b.getDataOrcamento() == null) return 0;
                            if (a.getDataOrcamento() == null) return 1;
                            if (b.getDataOrcamento() == null) return -1;
                            int c = b.getDataOrcamento().compareTo(a.getDataOrcamento());
                            if (c != 0) return c;
                            Integer idA = a.getId();
                            Integer idB = b.getId();
                            if (idA == null && idB == null) return 0;
                            if (idA == null) return 1;
                            if (idB == null) return -1;
                            return idB.compareTo(idA);
                        })
                        .map(OrdemServicoMapper::paraOrdemServicoResumo)
                        .toList()
                : List.of();
        return VeiculoMapper.paraHistoricoVeiculoResponse(veiculo, ordens);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> consultarHistoricoPaginado(String placa, Pageable pageable) {
        HistoricoVeiculoResponse historico = consultarHistorico(placa);
        List<OrdemServicoResumo> ordens = historico.ordensServico();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), ordens.size());
        List<OrdemServicoResumo> subList = (start > ordens.size()) ? List.of() : ordens.subList(start, end);
        return new PageImpl<>(subList, pageable, ordens.size());
    }

    @Transactional(readOnly = true)
    public Veiculo buscarEntidade(Integer id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
    }

    @Transactional(readOnly = true)
    public Veiculo buscarEntidadePorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new RecursoNaoEncontradoException("Veículo não encontrado.");
        }
        String placaNormalizada = normalizarPlaca(placa);

        return veiculoRepository.findComClienteByPlacaIgnoreCase(placaNormalizada)
                .or(() -> {
                    String variante = alternarHifen(placaNormalizada);
                    return veiculoRepository.findComClienteByPlacaIgnoreCase(variante);
                })
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado."));
    }

    public static String normalizarPlaca(String placa) {
        if (placa == null) return null;
        return placa.trim().toUpperCase();
    }

    private String alternarHifen(String placa) {
        if (placa.contains("-")) {
            return placa.replace("-", "");
        } else if (placa.length() == 7) {
            return placa.substring(0, 3) + "-" + placa.substring(3);
        }
        return placa;
    }
}
