package com.cartoon.api.ordemServico.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.AtualizarItemValorRequest;
import com.cartoon.api.ordemServico.dto.request.DescontoRequest;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.mapper.OrdemServicoMapper;
import com.cartoon.api.ordemServico.models.Desconto;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.ordemServico.specs.OrdemServicoSpecs;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.service.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {
    private final OrdemServicoRepository ordemServicoRepository;
    private final VeiculoService veiculoService;
    private final UsuarioService usuarioService;
    private final PecaService pecaService;
    private final ServicoService servicoService;
    private final CalculadoraOrcamento calculadora;

    @Transactional
    public OrdemServicoResponse salvar(OrdemServicoRequest request, UsuarioAutenticado solicitante) {
        Veiculo veiculo = veiculoService.buscarEntidade(request.veiculoId());
        Oficina oficina = obterOficinaDoSolicitante(solicitante);

        Usuario mecanico = usuarioService.buscarEntidade(request.mecanicoId());
        if (mecanico.getRole() != Role.MECANICO) {
            throw new RegraDeNegocioException("O administrador não pode se responsabilizar pelo serviço");
        }

        OrdemServico os = OrdemServicoMapper.paraOrdemServico(request, veiculo, oficina, mecanico);
        os.setDataOrcamento(LocalDate.now());
        os.recalcularTotal();
        os = ordemServicoRepository.save(os);

        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> listar(OrdemServicoFiltro filtro, Pageable pageable) {
        return listar(filtro, pageable, null);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> listar(OrdemServicoFiltro filtro, Pageable pageable, UsuarioAutenticado solicitante) {
        OrdemServicoFiltro filtroEfetivo = filtro != null
                ? filtro
                : new OrdemServicoFiltro(null, null, null, null, null, null);

        if (filtroEfetivo.oficinaId() == null && solicitante != null && solicitante.id() != null
                && !"SUPERADMIN".equalsIgnoreCase(solicitante.role())) {
            Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
            if (usuario.getOficina() != null) {
                filtroEfetivo = new OrdemServicoFiltro(
                        usuario.getOficina().getId(),
                        filtroEfetivo.veiculoId(),
                        filtroEfetivo.placa(),
                        filtroEfetivo.mecanicoId(),
                        filtroEfetivo.statusServico(),
                        filtroEfetivo.status()
                );
            }
        }

        Specification<OrdemServico> spec = OrdemServicoSpecs.montarFiltros(filtroEfetivo);
        return ordemServicoRepository.findAll(spec, pageable).map(OrdemServicoMapper::paraOrdemServicoResumo);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResumo> buscarHistoricoPorPlaca(String placa) {
        return ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(placa)
                .stream()
                .map(OrdemServicoMapper::paraOrdemServicoResumo)
                .toList();
    }

    @Transactional
    public void excluir(Integer id) {
        OrdemServico os = buscarEntidade(id);
        validarNaoFinalizada(os);
        ordemServicoRepository.delete(os);
    }

    @Transactional
    public OrdemServicoResponse aceitar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.ACEITO);
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse iniciar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.EM_ANDAMENTO);
        os.setDataInicio(LocalDate.now());
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.FINALIZADO);
        os.setDataFinalizacao(LocalDate.now());
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse rejeitar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.REJEITADO);
        os.setDataRejeicao(LocalDate.now());
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarPeca(Integer id, ItemPecaRequest request) {
        OrdemServico os = buscarEntidade(id);
        Peca peca = pecaService.buscarEntidade(request.pecaId());

        validarNaoFinalizada(os);
        if (peca.getOficina() != null && peca.getOficina().getId() != null
                && os.getOficina() != null && os.getOficina().getId() != null
                && !peca.getOficina().getId().equals(os.getOficina().getId())) {
            throw new RegraDeNegocioException("A peça informada não pertence à oficina desta ordem de serviço.");
        }

        BigDecimal margemPeca = (os.getOficina() != null && os.getOficina().getMargemPeca() != null)
                ? os.getOficina().getMargemPeca()
                : BigDecimal.ONE;
        BigDecimal valorUnitario = calculadora.valorPeca(peca.getValorBase(), margemPeca);

        ItemPeca itemPeca = OrdemServicoMapper.paraItemPeca(request, peca, valorUnitario, margemPeca);
        os.adicionarItemPeca(itemPeca);
        os.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(os);
        if (salvo != null) {
            os = salvo;
        }

        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse removerPeca(Integer ordemId, Integer itemId) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarNaoFinalizada(ordem);

        ItemPeca item = ordem.getItensPeca().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de peça", itemId));

        ordem.removerItemPeca(item);
        ordem.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(ordem);
        if (salvo != null) {
            ordem = salvo;
        }
        return OrdemServicoMapper.paraOrdemServicoResponse(ordem);
    }

    @Transactional
    public OrdemServicoResponse adicionarServico(Integer id, ItemServicoRequest request) {
        OrdemServico os = buscarEntidade(id);
        Servico servico = servicoService.buscarEntidade(request.servicoId());

        validarNaoFinalizada(os);
        if (servico.getOficina() != null && servico.getOficina().getId() != null
                && os.getOficina() != null && os.getOficina().getId() != null
                && !servico.getOficina().getId().equals(os.getOficina().getId())) {
            throw new RegraDeNegocioException("O serviço informado não pertence à oficina desta ordem de serviço.");
        }

        BigDecimal fipe = os.getVeiculo().getValorFipe();
        if (fipe == null) {
            throw new RegraDeNegocioException(
                    "Veículo sem valor FIPE cadastrado: não é possível calcular o serviço.");
        }
        Double horasDecimais;
        if (servico.getTempo() != null) {
            horasDecimais = calculadora.converterParaDecimal(servico.getTempo());
        } else if (servico.getQuantidadeHoras() != null) {
            horasDecimais = servico.getQuantidadeHoras();
        } else {
            throw new RegraDeNegocioException(
                    "Serviço sem quantidade de horas cadastrada: não é possível calcular o serviço.");
        }

        Oficina oficina = os.getOficina();
        if (oficina == null || oficina.getValorHora() == null) {
            throw new RegraDeNegocioException(
                    "Oficina sem valor por hora cadastrado: não é possível calcular o serviço.");
        }

        BigDecimal taxaRisco = oficina.getTaxaRisco() != null ? oficina.getTaxaRisco() : BigDecimal.ZERO;
        BigDecimal pesoServico = request.pesoServico() != null
                ? request.pesoServico()
                : (servico.getPesoServicoPadrao() != null ? servico.getPesoServicoPadrao() : new BigDecimal("0.5"));

        BigDecimal valorUnitario = calculadora.valorServico(
                horasDecimais,
                oficina.getValorHora(),
                fipe,
                taxaRisco,
                pesoServico
        );

        ItemServico itemServico = OrdemServicoMapper.paraItemServico(request, servico);
        itemServico.setPesoServico(pesoServico);
        itemServico.setValorUnitario(valorUnitario);
        itemServico.calcSubtotal();
        os.adicionarItemServico(itemServico);
        os.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(os);
        if (salvo != null) {
            os = salvo;
        }

        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse removerServico(Integer ordemId, Integer itemId) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarNaoFinalizada(ordem);

        ItemServico item = ordem.getItensServico().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de serviço", itemId));

        ordem.removerItemServico(item);
        ordem.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(ordem);
        if (salvo != null) {
            ordem = salvo;
        }
        return OrdemServicoMapper.paraOrdemServicoResponse(ordem);
    }

    @Transactional
    public OrdemServicoResponse atualizarItemServico(Integer ordemId, Integer itemId, AtualizarItemValorRequest request) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarNaoFinalizada(ordem);

        ItemServico item = ordem.getItensServico().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de serviço", itemId));

        if (request == null || (request.pesoServico() == null && request.valorUnitario() == null)) {
            throw new RegraDeNegocioException("Informe o novo peso do serviço ou o novo valor unitário.");
        }

        if (request.pesoServico() != null) {
            item.setPesoServico(request.pesoServico());
            if (request.valorUnitario() == null) {
                BigDecimal fipe = ordem.getVeiculo() != null ? ordem.getVeiculo().getValorFipe() : null;
                Double horasDecimais = item.getTempo() != null
                        ? calculadora.converterParaDecimal(item.getTempo())
                        : (item.getQuantidadeHoras() != null ? item.getQuantidadeHoras() : 1.0);
                Oficina oficina = ordem.getOficina();
                BigDecimal valorHora = (oficina != null && oficina.getValorHora() != null)
                        ? oficina.getValorHora()
                        : BigDecimal.ZERO;
                BigDecimal taxaRisco = (oficina != null && oficina.getTaxaRisco() != null)
                        ? oficina.getTaxaRisco()
                        : BigDecimal.ZERO;

                if (fipe != null && valorHora.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal recalculado = calculadora.valorServico(
                            horasDecimais,
                            valorHora,
                            fipe,
                            taxaRisco,
                            request.pesoServico()
                    );
                    item.setValorUnitario(recalculado);
                }
            }
        }

        if (request.valorUnitario() != null) {
            if (request.valorUnitario().compareTo(BigDecimal.ZERO) < 0) {
                throw new RegraDeNegocioException("O valor unitário do serviço deve ser maior ou igual a zero.");
            }
            item.setValorUnitario(request.valorUnitario().setScale(2, RoundingMode.HALF_UP));
        }

        item.calcSubtotal();
        ordem.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(ordem);
        if (salvo != null) {
            ordem = salvo;
        }
        return OrdemServicoMapper.paraOrdemServicoResponse(ordem);
    }

    @Transactional
    public OrdemServicoResponse atualizarValorItemServico(Integer ordemId, Integer itemId, BigDecimal novoValorUnitario) {
        return atualizarItemServico(ordemId, itemId, new AtualizarItemValorRequest(novoValorUnitario, null));
    }

    @Transactional
    public OrdemServicoResponse atualizarValorItemPeca(Integer ordemId, Integer itemId, BigDecimal novoValorUnitario) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarNaoFinalizada(ordem);

        ItemPeca item = ordem.getItensPeca().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de peça", itemId));

        if (novoValorUnitario == null || novoValorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("O valor unitário da peça deve ser maior ou igual a zero.");
        }

        item.setValorUnitario(novoValorUnitario.setScale(2, RoundingMode.HALF_UP));
        item.calcSubtotal();

        BigDecimal base = item.getValorBase();
        if (base == null && item.getPeca() != null) {
            base = item.getPeca().getValorBase();
        }
        if (base != null && base.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal novaMargem = item.getValorUnitario().divide(base, 4, RoundingMode.HALF_UP);
            item.setMargem(novaMargem);
        }

        ordem.recalcularTotal();
        OrdemServico salvo = ordemServicoRepository.save(ordem);
        if (salvo != null) {
            ordem = salvo;
        }
        return OrdemServicoMapper.paraOrdemServicoResponse(ordem);
    }

    @Transactional
    public OrdemServicoResponse aplicarDesconto(Integer id, DescontoRequest request) {
        OrdemServico os = buscarEntidade(id);
        validarNaoFinalizada(os);

        os.setDesconto(request.desconto() != null ? request.desconto() : Desconto.SEM_DESCONTO);
        os.recalcularTotal();
        ordemServicoRepository.save(os);

        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarEntidade(Integer id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico", id));
    }

    public void validarNaoFinalizada(OrdemServico os) {
        if (os.getStatusServico() == StatusServico.FINALIZADO) {
            throw new ConflitoException("Não é possível alterar uma ordem finalizada");
        }
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResumo> listarHistoricoPorVeiculoId(Integer veiculoId) {
        return ordemServicoRepository.findByVeiculoIdOrderByDataOrcamentoDescIdDesc(veiculoId).stream()
                .map(OrdemServicoMapper::paraOrdemServicoResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> listarHistoricoPorVeiculoIdPaginado(Integer veiculoId, Pageable pageable) {
        return ordemServicoRepository.findByVeiculoIdOrderByDataOrcamentoDescIdDesc(veiculoId, pageable)
                .map(OrdemServicoMapper::paraOrdemServicoResumo);
    }

    private Oficina obterOficinaDoSolicitante(UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.id() == null) {
            throw new RegraDeNegocioException("Usuário autenticado é obrigatório para identificar a oficina.");
        }
        Usuario usuarioLogado = usuarioService.buscarEntidade(solicitante.id().intValue());
        if (usuarioLogado.getOficina() == null) {
            throw new RegraDeNegocioException("O usuário autenticado não está vinculado a uma oficina.");
        }
        return usuarioLogado.getOficina();
    }
}
