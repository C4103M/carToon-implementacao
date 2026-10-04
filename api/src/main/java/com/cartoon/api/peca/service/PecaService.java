package com.cartoon.api.peca.service;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.peca.dto.PecaFiltro;
import com.cartoon.api.peca.dto.mapper.PecaMapper;
import com.cartoon.api.peca.dto.request.PecaRequest;
import com.cartoon.api.peca.dto.response.PecaResponse;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.repositories.PecaRepository;
import com.cartoon.api.peca.specs.PecaSpecs;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PecaService {
    private final PecaRepository pecaRepository;

    @Transactional(readOnly = true)
    public Peca buscarEntidade(Integer id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
    }

    @Transactional(readOnly = true)
    public PecaResponse buscar(Integer id) {
        return PecaMapper.paraPecaResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> listar(PecaFiltro filtro, Pageable pageable) {
        Specification<Peca> spec = PecaSpecs.montarFiltros(filtro);
        return pecaRepository.findAll(spec, pageable).map(PecaMapper::paraPecaResponse);
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> listar(Pageable pageable) {
        return listar(new PecaFiltro(null, null, null), pageable);
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> listar(String busca, String nome, String fabricante, Pageable pageable) {
        return listar(new PecaFiltro(busca, nome, fabricante), pageable);
    }

    @Transactional
    public PecaResponse salvar(PecaRequest request) {
        Peca peca = PecaMapper.paraPeca(request);
        return PecaMapper.paraPecaResponse(pecaRepository.save(peca));
    }

    @Transactional
    public PecaResponse atualizar(Integer id, PecaRequest request) {
        Peca peca = buscarEntidade(id);
        PecaMapper.atualizarEntidade(peca, request);
        return PecaMapper.paraPecaResponse(pecaRepository.save(peca));
    }

    @Transactional
    public void deletar(Integer id) {
        Peca peca = buscarEntidade(id);
        pecaRepository.delete(peca);
    }
}
