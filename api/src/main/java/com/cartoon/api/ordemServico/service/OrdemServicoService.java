package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.mapper.OrdemServicoMapper;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.ordemServico.specs.OrdemServicoSpecs;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {
    private final OrdemServicoRepository ordemServicoRepository;
    private final VeiculoService veiculoService;
    private final UsuarioService usuarioService;
    private final OficinaService oficinaService;
    private final PecaService pecaService;

    @Transactional
    public OrdemServicoResponse salvar(OrdemServicoRequest request) {
        Veiculo veiculo = veiculoService.buscarEntidade(request.veiculoId());
        Oficina oficina = oficinaService.buscarEntidade(request.oficinaId());
        Usuario mecanico = usuarioService.buscarEntidade(request.mecanicoId());
        OrdemServico os = OrdemServicoMapper.paraOrdemServico(request, veiculo, oficina, mecanico);
        os.setDataOrcamento(LocalDate.now());
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
        Specification<OrdemServico> spec = OrdemServicoSpecs.montarFiltros(filtro);
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

        ItemPeca itemPeca = OrdemServicoMapper.paraItemPeca(request, peca);
        os.adicionarItemPeca(itemPeca);

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
        return OrdemServicoMapper.paraOrdemServicoResponse(ordem);
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
}
