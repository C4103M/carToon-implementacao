package com.cartoon.api.ordemServico.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.model.Cliente;
import org.springframework.security.access.AccessDeniedException;
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
import com.cartoon.api.seguranca.TenantGuard;
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
import org.springframework.security.access.AccessDeniedException;
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
    private final TenantGuard tenantGuard;

    @Transactional
    public OrdemServicoResponse salvar(OrdemServicoRequest request, UsuarioAutenticado solicitante) {
        Veiculo veiculo = veiculoService.buscarEntidade(request.veiculoId());
        Oficina oficina = obterOficinaDoSolicitante(solicitante);

        if (Boolean.FALSE.equals(oficina.getAtivo())) {
            throw new RegraDeNegocioException("Não é possível abrir ordem de serviço para uma oficina inativa.");
        }

        Usuario mecanico = usuarioService.buscarEntidade(request.mecanicoId());
        if (mecanico.getRole() != Role.MECANICO) {
            throw new RegraDeNegocioException("O administrador não pode se responsabilizar pelo serviço");
        }
        if (Boolean.FALSE.equals(mecanico.getAtivo())) {
            throw new RegraDeNegocioException("O mecânico informado está inativo.");
        }
        if (mecanico.getOficina() == null || !mecanico.getOficina().getId().equals(oficina.getId())) {
            throw new RegraDeNegocioException("O mecânico informado pertence a outra oficina.");
        }

        if (Boolean.FALSE.equals(veiculo.getAtivo())) {
            throw new RegraDeNegocioException("O veículo informado está inativo.");
        }
        Cliente cliente = veiculo.getCliente();
        if (cliente != null) {
            if (Boolean.FALSE.equals(cliente.isAtivo())) {
                throw new RegraDeNegocioException("O cliente proprietário do veículo está inativo.");
            }
            if (cliente.getOficina() != null && !cliente.getOficina().getId().equals(oficina.getId())) {
                throw new RegraDeNegocioException("O veículo pertence a um cliente de outra oficina.");
            }
        }

        OrdemServico os = OrdemServicoMapper.paraOrdemServico(request, veiculo, oficina, mecanico);
        os.setDataOrcamento(LocalDate.now());
        os.recalcularTotal();
        os = ordemServicoRepository.save(os);

        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscar(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> listar(OrdemServicoFiltro filtro, Pageable pageable, UsuarioAutenticado solicitante) {
        OrdemServicoFiltro filtroEfetivo = filtro != null
                ? filtro
                : new OrdemServicoFiltro(null, null, null, null, null, null);

        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficinaSolicitante = obterOficinaDoSolicitante(solicitante);
            filtroEfetivo = new OrdemServicoFiltro(
                    oficinaSolicitante.getId(),
                    filtroEfetivo.veiculoId(),
                    filtroEfetivo.placa(),
                    filtroEfetivo.mecanicoId(),
                    filtroEfetivo.statusServico(),
                    filtroEfetivo.status()
            );
        }

        Specification<OrdemServico> spec = OrdemServicoSpecs.montarFiltros(filtroEfetivo);
        return ordemServicoRepository.findAll(spec, pageable).map(OrdemServicoMapper::paraOrdemServicoResumo);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResumo> buscarHistoricoPorPlaca(String placa, UsuarioAutenticado solicitante) {
        List<OrdemServico> ordens = ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(placa);
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            ordens = ordens.stream()
                    .filter(os -> os.getOficina() != null && os.getOficina().getId().equals(oficina.getId()))
                    .toList();
        }
        return ordens.stream().map(OrdemServicoMapper::paraOrdemServicoResumo).toList();
    }

    @Transactional
    public void excluir(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        if (os.getStatusServico() == StatusServico.FINALIZADO || os.getStatusServico() == StatusServico.EM_ANDAMENTO) {
            throw new ConflitoException("Não é possível excluir uma ordem de serviço em andamento ou finalizada.");
        }
        ordemServicoRepository.delete(os);
    }

    @Transactional
    public OrdemServicoResponse aceitar(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        if (os.getStatusServico() != StatusServico.PENDENTE) {
            throw new ConflitoException("A ordem de serviço só pode ser aceita se estiver com status PENDENTE (status atual: " + os.getStatusServico() + ").");
        }
        os.setStatusServico(StatusServico.ACEITO);
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse iniciar(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        if (os.getStatusServico() != StatusServico.ACEITO) {
            throw new ConflitoException("A ordem de serviço só pode ser iniciada se estiver com status ACEITO (status atual: " + os.getStatusServico() + ").");
        }
        os.setStatusServico(StatusServico.EM_ANDAMENTO);
        if (os.getDataInicio() == null) {
            os.setDataInicio(LocalDate.now());
        }
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        if (os.getStatusServico() != StatusServico.EM_ANDAMENTO) {
            throw new ConflitoException("A ordem de serviço só pode ser finalizada se estiver com status EM_ANDAMENTO (status atual: " + os.getStatusServico() + ").");
        }
        boolean semItens = (os.getItensPeca() == null || os.getItensPeca().isEmpty())
                && (os.getItensServico() == null || os.getItensServico().isEmpty());
        if (semItens) {
            throw new RegraDeNegocioException("Não é possível finalizar uma ordem de serviço sem itens.");
        }
        os.setStatusServico(StatusServico.FINALIZADO);
        if (os.getDataFinalizacao() == null) {
            os.setDataFinalizacao(LocalDate.now());
        }
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse rejeitar(Integer id, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        if (os.getStatusServico() != StatusServico.PENDENTE && os.getStatusServico() != StatusServico.ACEITO) {
            throw new ConflitoException("A ordem de serviço só pode ser rejeitada a partir do status PENDENTE ou ACEITO (status atual: " + os.getStatusServico() + ").");
        }
        os.setStatusServico(StatusServico.REJEITADO);
        if (os.getDataRejeicao() == null) {
            os.setDataRejeicao(LocalDate.now());
        }
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarPeca(Integer id, ItemPecaRequest request, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        validarPodeModificarItens(os);
        Peca peca = pecaService.buscarEntidade(request.pecaId());
        if (Boolean.FALSE.equals(peca.getAtivo())) {
            throw new RegraDeNegocioException("Não é possível adicionar uma peça inativa à ordem de serviço.");
        }

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
    public OrdemServicoResponse removerPeca(Integer ordemId, Integer itemId, UsuarioAutenticado solicitante) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarAcessoOficina(ordem, solicitante);
        validarPodeModificarItens(ordem);

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
    public OrdemServicoResponse adicionarServico(Integer id, ItemServicoRequest request, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        validarPodeModificarItens(os);
        Servico servico = servicoService.buscarEntidade(request.servicoId());
        if (Boolean.FALSE.equals(servico.getAtivo())) {
            throw new RegraDeNegocioException("Não é possível adicionar um serviço inativo à ordem de serviço.");
        }

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
    public OrdemServicoResponse removerServico(Integer ordemId, Integer itemId, UsuarioAutenticado solicitante) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarAcessoOficina(ordem, solicitante);
        validarPodeModificarItens(ordem);

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
    public OrdemServicoResponse atualizarItemServico(Integer ordemId, Integer itemId, AtualizarItemValorRequest request, UsuarioAutenticado solicitante) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarAcessoOficina(ordem, solicitante);
        validarPodeModificarItens(ordem);

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
            if (solicitante != null && !solicitante.isAdmin() && !solicitante.isSuperAdmin()) {
                throw new AccessDeniedException("Apenas administradores podem definir diretamente o valor monetário do serviço. Mecânicos podem apenas ajustar o peso do serviço.");
            }
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
    public OrdemServicoResponse atualizarValorItemServico(Integer ordemId, Integer itemId, BigDecimal novoValorUnitario, UsuarioAutenticado solicitante) {
        return atualizarItemServico(ordemId, itemId, new AtualizarItemValorRequest(novoValorUnitario, null), solicitante);
    }

    @Transactional
    public OrdemServicoResponse atualizarValorItemPeca(Integer ordemId, Integer itemId, BigDecimal novoValorUnitario, UsuarioAutenticado solicitante) {
        OrdemServico ordem = buscarEntidade(ordemId);
        validarAcessoOficina(ordem, solicitante);
        validarPodeModificarItens(ordem);

        ItemPeca item = ordem.getItensPeca().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de peça", itemId));

        if (novoValorUnitario == null || novoValorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("O valor unitário da peça deve ser maior ou igual a zero.");
        }

        if (solicitante != null && !solicitante.isAdmin() && !solicitante.isSuperAdmin()) {
            BigDecimal base = item.getValorBase();
            if (base == null && item.getPeca() != null) {
                base = item.getPeca().getValorBase();
            }
            if (base != null && novoValorUnitario.compareTo(base) < 0) {
                throw new RegraDeNegocioException("Mecânicos não têm permissão para vender peças abaixo do custo base (R$ " + base + ").");
            }
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
    public OrdemServicoResponse aplicarDesconto(Integer id, DescontoRequest request, UsuarioAutenticado solicitante) {
        OrdemServico os = buscarEntidade(id);
        validarAcessoOficina(os, solicitante);
        validarPodeModificarItens(os);

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

    public void validarPodeModificarItens(OrdemServico os) {
        if (os.getStatusServico() != StatusServico.PENDENTE) {
            throw new ConflitoException("Não é possível alterar itens ou valores de uma ordem com status " + os.getStatusServico() + ". Modificações são permitidas apenas quando PENDENTE.");
        }
    }

    public void validarNaoFinalizada(OrdemServico os) {
        validarPodeModificarItens(os);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResumo> listarHistoricoPorVeiculoId(Integer veiculoId, UsuarioAutenticado solicitante) {
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            return ordemServicoRepository.findByVeiculoIdAndOficinaIdOrderByDataOrcamentoDescIdDesc(veiculoId, oficina.getId())
                    .stream()
                    .map(OrdemServicoMapper::paraOrdemServicoResumo)
                    .toList();
        }
        return ordemServicoRepository.findByVeiculoIdOrderByDataOrcamentoDescIdDesc(veiculoId).stream()
                .map(OrdemServicoMapper::paraOrdemServicoResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumo> listarHistoricoPorVeiculoIdPaginado(Integer veiculoId, Pageable pageable, UsuarioAutenticado solicitante) {
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            return ordemServicoRepository.findByVeiculoIdAndOficinaIdOrderByDataOrcamentoDescIdDesc(veiculoId, oficina.getId(), pageable)
                    .map(OrdemServicoMapper::paraOrdemServicoResumo);
        }
        return ordemServicoRepository.findByVeiculoIdOrderByDataOrcamentoDescIdDesc(veiculoId, pageable)
                .map(OrdemServicoMapper::paraOrdemServicoResumo);
    }

    public void validarAcessoOficina(OrdemServico os, UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.isSuperAdmin()) {
            return;
        }
        tenantGuard.validarAcessoOficina(os.getOficina(), solicitante,
                "Você não tem permissão para acessar ou alterar ordens de serviço de outra oficina.");
    }

    private Oficina obterOficinaDoSolicitante(UsuarioAutenticado solicitante) {
        if (tenantGuard != null) {
            try {
                Oficina of = tenantGuard.obterOficinaDoSolicitante(solicitante);
                if (of != null) {
                    return of;
                }
            } catch (Exception ignored) {
            }
        }
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
