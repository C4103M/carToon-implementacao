package com.cartoon.api.veiculo;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.PlacaInvalidaException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.veiculo.dto.mapper.VeiculoMapper;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.specs.VeiculoSpecs;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class VeiculoService {

    private static final Pattern PADRAO_PLACA = Pattern.compile(
            "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$"
    );

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;
    private final OrdemServicoService ordemServicoService;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          ClienteService clienteService,
                          @Lazy OrdemServicoService ordemServicoService) {
        this.veiculoRepository = veiculoRepository;
        this.clienteService = clienteService;
        this.ordemServicoService = ordemServicoService;
    }

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
        List<OrdemServicoResumo> ordens = ordemServicoService.buscarHistoricoPorPlaca(veiculo.getPlaca());
        return VeiculoMapper.paraHistoricoVeiculoResponse(veiculo, ordens);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> consultarHistoricoPaginado(String placa, Pageable pageable) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        OrdemServicoFiltro filtro = new OrdemServicoFiltro(null, null, veiculo.getPlaca(), null, null);
        return ordemServicoService.listar(filtro, pageable);
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
