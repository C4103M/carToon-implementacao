package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.oficina.OficinaService;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.mapper.OrdemServicoMapper;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.ordemServico.specs.OrdemServicoSpecs;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.peca.PecaService;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioService;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoService;
import com.cartoon.api.veiculo.dto.mapper.VeiculoMapper;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResumo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {
    private final OrdemServicoRepository ordemServicoRepository;

    private final UsuarioService usuarioService;
    private final VeiculoService veiculoService;
    private final OficinaService oficinaService;
    private final PecaService pecaService;

    @Transactional
    public OrdemServicoResponse salvar(OrdemServicoRequest request) {
        Veiculo veiculo = veiculoService.buscarEntidade(request.veiculoId());
        Oficina oficina = oficinaService.buscarEntidade(request.oficinaId());
        Usuario mecanico = usuarioService.buscarEntidade(request.mecanicoId());
        OrdemServico os = OrdemServicoMapper.paraOrdemServico(request, veiculo, oficina, mecanico);
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

    @Transactional
    public void excluir(Integer id) throws Exception {
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
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.FINALIZADO);
        ordemServicoRepository.save(os);
        return OrdemServicoMapper.paraOrdemServicoResponse(os);
    }

    @Transactional
    public OrdemServicoResponse rejeitar(Integer id) {
        OrdemServico os = buscarEntidade(id);
        os.setStatusServico(StatusServico.REJEITADO);
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

    @Transactional(readOnly = true)
    public HistoricoVeiculoResponse consultarHistoricoPorPlaca(String placa) {
        Veiculo veiculo = veiculoService.buscarEntidadePorPlaca(placa);
        List<OrdemServico> ordens = ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(veiculo.getPlaca());
        VeiculoResumo veiculoResumo = VeiculoMapper.paraVeiculoResumo(veiculo);

        if (ordens.isEmpty()) {
            return new HistoricoVeiculoResponse(
                    veiculoResumo,
                    "Nenhuma ordem de serviço encontrada para este veículo.",
                    List.of(),
                    0
            );
        }

        List<OrdemServicoHistoricoResponse> ordensDTO = ordens.stream()
                .map(OrdemServicoMapper::paraOrdemServicoHistoricoResponse)
                .toList();

        return new HistoricoVeiculoResponse(
                veiculoResumo,
                "Histórico recuperado com sucesso.",
                ordensDTO,
                ordensDTO.size()
        );
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoHistoricoResponse> consultarHistoricoPaginadoPorPlaca(String placa, Pageable pageable) {
        Veiculo veiculo = veiculoService.buscarEntidadePorPlaca(placa);
        return ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(veiculo.getPlaca(), pageable)
                .map(OrdemServicoMapper::paraOrdemServicoHistoricoResponse);
    }

    @Transactional(readOnly = true)
    public OrdemServicoHistoricoResponse detalharOrdem(Integer id) {
        OrdemServico os = ordemServicoRepository.findDetalhadaById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço", id));
        return OrdemServicoMapper.paraOrdemServicoHistoricoResponse(os);
    }

    public void validarNaoFinalizada(OrdemServico os) {
        if (os.getStatusServico() == StatusServico.FINALIZADO) {
            throw new ConflitoException("Não é possível alterar uma ordem finalizada");
        }
    }
}
