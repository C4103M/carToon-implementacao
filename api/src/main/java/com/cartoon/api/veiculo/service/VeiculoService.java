package com.cartoon.api.veiculo.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.PlacaInvalidaException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.fipe.dto.FipeItemDTO;
import com.cartoon.api.fipe.dto.FipeValorDTO;
import com.cartoon.api.fipe.dto.TipoVeiculo;
import com.cartoon.api.fipe.service.FipeService;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.mapper.OrdemServicoMapper;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.seguranca.TenantGuard;
import com.cartoon.api.veiculo.dto.mapper.VeiculoMapper;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.repository.VeiculoRepository;
import com.cartoon.api.veiculo.specs.VeiculoSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class VeiculoService {

    private static final Pattern PADRAO_PLACA = Pattern.compile(
            "^([A-Za-z]{3}-?[0-9]{4}|[A-Za-z]{3}-?[0-9][A-Za-z][0-9]{2})$"
    );

    private static final List<StatusServico> STATUS_EM_ABERTO = List.of(
            StatusServico.PENDENTE, StatusServico.ACEITO, StatusServico.EM_ANDAMENTO
    );

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;
    private final OrdemServicoService ordemServicoService;
    private final FipeService fipeService;
    private final TenantGuard tenantGuard;
    private final OrdemServicoRepository ordemServicoRepository;

    public VeiculoService(
            VeiculoRepository veiculoRepository,
            ClienteService clienteService,
            @Lazy OrdemServicoService ordemServicoService,
            @Lazy FipeService fipeService,
            TenantGuard tenantGuard,
            @Lazy @Autowired(required = false) OrdemServicoRepository ordemServicoRepository
    ) {
        this.veiculoRepository = veiculoRepository;
        this.clienteService = clienteService;
        this.ordemServicoService = ordemServicoService;
        this.fipeService = fipeService;
        this.tenantGuard = tenantGuard;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public VeiculoService(
            VeiculoRepository veiculoRepository,
            ClienteService clienteService,
            OrdemServicoService ordemServicoService,
            FipeService fipeService,
            TenantGuard tenantGuard
    ) {
        this(veiculoRepository, clienteService, ordemServicoService, fipeService, tenantGuard, null);
    }

    @Transactional
    public VeiculoResponse cadastrar(VeiculoRequest request, UsuarioAutenticado solicitante) {
        Cliente cliente = clienteService.buscarEntidade(request.clienteId());
        if (!cliente.isAtivo()) {
            throw new ClienteInativoException();
        }

        if (solicitante != null && !solicitante.isSuperAdmin() && tenantGuard != null) {
            tenantGuard.validarAcessoOficina(cliente.getOficina(), solicitante,
                    "Você não pode cadastrar veículos para clientes de outra oficina.");
        }

        if (request.placa() == null || !PADRAO_PLACA.matcher(request.placa().trim()).matches()) {
            throw new PlacaInvalidaException();
        }

        String placaNormalizada = normalizarPlaca(request.placa());
        boolean placaExiste;
        if (cliente.getOficina() != null && cliente.getOficina().getId() != null) {
            placaExiste = veiculoRepository.existsByClienteOficinaIdAndPlacaIgnoreCase(cliente.getOficina().getId(), placaNormalizada);
        } else {
            placaExiste = veiculoRepository.existsByPlacaIgnoreCase(placaNormalizada);
        }
        if (placaExiste) {
            throw new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema.");
        }

        Veiculo veiculo = VeiculoMapper.paraVeiculo(request, cliente);
        veiculo.setPlaca(placaNormalizada);
        veiculo = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse atualizar(Integer id, VeiculoAtualizacaoRequest request, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidade(id);
        validarAcessoOficina(veiculo, solicitante);

        Cliente novoCliente = null;
        if (request.clienteId() != null && !request.clienteId().equals(veiculo.getCliente().getId())) {
            novoCliente = clienteService.buscarEntidade(request.clienteId());
            if (!novoCliente.isAtivo()) {
                throw new ClienteInativoException();
            }
            if (solicitante != null && !solicitante.isSuperAdmin() && tenantGuard != null) {
                tenantGuard.validarAcessoOficina(novoCliente.getOficina(), solicitante,
                        "Você não pode transferir o veículo para um cliente de outra oficina.");
            }
        }

        if (request.placa() != null && !request.placa().isBlank()) {
            if (!PADRAO_PLACA.matcher(request.placa().trim()).matches()) {
                throw new PlacaInvalidaException();
            }
            String placaNormalizada = normalizarPlaca(request.placa());
            Cliente clienteEfetivo = novoCliente != null ? novoCliente : veiculo.getCliente();
            Integer oficinaId = (clienteEfetivo != null && clienteEfetivo.getOficina() != null) ? clienteEfetivo.getOficina().getId() : null;
            boolean placaExiste;
            if (oficinaId != null) {
                placaExiste = veiculoRepository.existsByClienteOficinaIdAndPlacaIgnoreCaseAndIdNot(oficinaId, placaNormalizada, id);
            } else {
                placaExiste = veiculoRepository.existsByPlacaIgnoreCaseAndIdNot(placaNormalizada, id);
            }
            if (placaExiste) {
                throw new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema.");
            }
        }

        if (request.ativo() != null && !request.ativo().equals(veiculo.getAtivo()) && Boolean.FALSE.equals(request.ativo())) {
            validarNaoPossuiOsAberta(id);
        }

        VeiculoMapper.atualizarDados(veiculo, request, novoCliente);
        veiculo = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional
    public VeiculoResponse atualizarValorFipe(Integer id, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidade(id);
        validarAcessoOficina(veiculo, solicitante);

        if (fipeService != null && veiculo.getMarca() != null && veiculo.getModelo() != null && veiculo.getAnoModelo() != null) {
            try {
                TipoVeiculo tipo = TipoVeiculo.CARROS;
                FipeItemDTO marcaEncontrada = null;
                for (FipeItemDTO m : fipeService.listarMarcas(tipo)) {
                    if (m.nome().trim().equalsIgnoreCase(veiculo.getMarca().trim())) {
                        marcaEncontrada = m;
                        break;
                    }
                }

                if (marcaEncontrada != null) {
                    FipeItemDTO modeloEncontrado = null;
                    for (FipeItemDTO m : fipeService.listarModelos(tipo, marcaEncontrada.codigo())) {
                        String mNome = m.nome().trim().toLowerCase();
                        String vModelo = veiculo.getModelo().trim().toLowerCase();
                        if (mNome.equals(vModelo) || mNome.contains(vModelo) || vModelo.contains(mNome)) {
                            modeloEncontrado = m;
                            break;
                        }
                    }

                    if (modeloEncontrado != null) {
                        List<FipeItemDTO> anos = fipeService.listarAnos(tipo, marcaEncontrada.codigo(), modeloEncontrado.codigo());
                        FipeItemDTO anoEncontrado = null;
                        for (FipeItemDTO a : anos) {
                            if (a.nome().startsWith(String.valueOf(veiculo.getAnoModelo()))
                                    || a.codigo().startsWith(String.valueOf(veiculo.getAnoModelo()))) {
                                anoEncontrado = a;
                                break;
                            }
                        }
                        if (anoEncontrado == null && !anos.isEmpty()) {
                            anoEncontrado = anos.get(0);
                        }

                        if (anoEncontrado != null) {
                            FipeValorDTO dadosFipe = fipeService.consultarValor(
                                    tipo,
                                    marcaEncontrada.codigo(),
                                    modeloEncontrado.codigo(),
                                    anoEncontrado.codigo()
                            );

                            if (dadosFipe != null && dadosFipe.valorFipe() != null) {
                                veiculo.setValorFipe(dadosFipe.valorFipe());
                                if (dadosFipe.mesReferencia() != null && !dadosFipe.mesReferencia().isBlank()) {
                                    veiculo.setMesReferencia(dadosFipe.mesReferencia());
                                }
                                if (dadosFipe.codigoFipe() != null && !dadosFipe.codigoFipe().isBlank()) {
                                    veiculo.setCodigoFipe(dadosFipe.codigoFipe());
                                }
                                veiculo = veiculoRepository.save(veiculo);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // Se o serviço FIPE externo estiver indisponível no momento, preserva o valor já salvo
            }
        }

        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Integer id, UsuarioAutenticado solicitante) {
        Veiculo veiculo = veiculoRepository.findComClienteById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
        validarAcessoOficina(veiculo, solicitante);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorPlaca(String placa, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        validarAcessoOficina(veiculo, solicitante);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public Page<VeiculoResponse> listar(VeiculoFiltro filtro, Pageable pageable, UsuarioAutenticado solicitante) {
        Specification<Veiculo> spec = VeiculoSpecs.montarFiltros(filtro);
        if (solicitante != null && !solicitante.isSuperAdmin() && tenantGuard != null) {
            Oficina oficina = tenantGuard.obterOficinaDoSolicitante(solicitante);
            spec = spec.and(VeiculoSpecs.porOficinaId(oficina.getId()));
        }
        return veiculoRepository.findAll(spec, pageable).map(VeiculoMapper::paraVeiculoResponse);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarPorCliente(Integer clienteId, UsuarioAutenticado solicitante) {
        Cliente cliente = clienteService.buscarEntidade(clienteId);
        if (solicitante != null && !solicitante.isSuperAdmin() && tenantGuard != null) {
            tenantGuard.validarAcessoOficina(cliente.getOficina(), solicitante,
                    "Você não tem permissão para visualizar veículos de clientes de outra oficina.");
        }
        return veiculoRepository.findByClienteId(clienteId).stream()
                .map(VeiculoMapper::paraVeiculoResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse inativar(Integer id, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidade(id);
        validarAcessoOficina(veiculo, solicitante);
        validarNaoPossuiOsAberta(id);
        veiculo.setAtivo(false);
        veiculo = veiculoRepository.save(veiculo);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    private void validarNaoPossuiOsAberta(Integer veiculoId) {
        if (ordemServicoRepository != null && ordemServicoRepository.existsByVeiculoIdAndStatusServicoIn(veiculoId, STATUS_EM_ABERTO)) {
            throw new ConflitoException("Não é possível inativar: o veículo possui ordens de serviço pendentes ou em andamento.");
        }
    }

    @Transactional
    public VeiculoResponse reativar(Integer id, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidade(id);
        validarAcessoOficina(veiculo, solicitante);
        veiculo.setAtivo(true);
        veiculo = veiculoRepository.save(veiculo);
        return VeiculoMapper.paraVeiculoResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public HistoricoVeiculoResponse consultarHistorico(String placa, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        validarAcessoOficina(veiculo, solicitante);

        List<OrdemServicoResumo> ordens = null;
        if (ordemServicoService != null && veiculo.getId() != null) {
            try {
                ordens = ordemServicoService.listarHistoricoPorVeiculoId(veiculo.getId(), solicitante);
            } catch (Exception ignored) {
            }
        }

        if ((ordens == null || ordens.isEmpty()) && veiculo.getOrdensServicos() != null && !veiculo.getOrdensServicos().isEmpty()) {
            ordens = veiculo.getOrdensServicos().stream()
                    .filter(os -> {
                        if (solicitante == null || solicitante.isSuperAdmin() || tenantGuard == null) {
                            return true;
                        }
                        Oficina oficinaSolicitante = tenantGuard.obterOficinaDoSolicitante(solicitante);
                        return os.getOficina() != null && os.getOficina().getId().equals(oficinaSolicitante.getId());
                    })
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
                    .toList();
        } else if (ordens == null) {
            ordens = List.of();
        }

        return VeiculoMapper.paraHistoricoVeiculoResponse(veiculo, ordens);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> consultarHistoricoPaginado(String placa, Pageable pageable, UsuarioAutenticado solicitante) {
        Veiculo veiculo = buscarEntidadePorPlaca(placa);
        validarAcessoOficina(veiculo, solicitante);
        if (ordemServicoService != null && veiculo.getId() != null) {
            return ordemServicoService.listarHistoricoPorVeiculoIdPaginado(veiculo.getId(), pageable, solicitante);
        }
        HistoricoVeiculoResponse historico = consultarHistorico(placa, solicitante);
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

    public void validarAcessoOficina(Veiculo veiculo, UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.isSuperAdmin() || tenantGuard == null) {
            return;
        }
        if (veiculo.getCliente() != null && veiculo.getCliente().getOficina() != null) {
            tenantGuard.validarAcessoOficina(veiculo.getCliente().getOficina(), solicitante,
                    "Você não tem permissão para acessar ou alterar veículos de outra oficina.");
        }
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
