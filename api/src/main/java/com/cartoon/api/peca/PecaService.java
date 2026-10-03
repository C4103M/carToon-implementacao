package com.cartoon.api.peca;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return PecaResponse.daEntidade(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> listar(String busca, String nome, String fabricante, Pageable pageable) {
        if (busca != null && !busca.isBlank()) {
            return pecaRepository.findByNomeContainingIgnoreCaseOrFabricanteContainingIgnoreCase(busca.trim(), busca.trim(), pageable)
                    .map(PecaResponse::daEntidade);
        }
        if (nome != null && !nome.isBlank() && fabricante != null && !fabricante.isBlank()) {
            return pecaRepository.findByNomeContainingIgnoreCaseAndFabricanteContainingIgnoreCase(nome.trim(), fabricante.trim(), pageable)
                    .map(PecaResponse::daEntidade);
        }
        if (nome != null && !nome.isBlank()) {
            return pecaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                    .map(PecaResponse::daEntidade);
        }
        if (fabricante != null && !fabricante.isBlank()) {
            return pecaRepository.findByFabricanteContainingIgnoreCase(fabricante.trim(), pageable)
                    .map(PecaResponse::daEntidade);
        }
        return pecaRepository.findAll(pageable).map(PecaResponse::daEntidade);
    }

    @Transactional(readOnly = true)
    public Page<PecaResponse> listar(Pageable pageable) {
        return listar(null, null, null, pageable);
    }

    @Transactional
    public PecaResponse salvar(PecaRequest request) {
        Peca peca = new Peca();
        peca.setNome(request.nome());
        peca.setFabricante(request.fabricante());
        peca.setValorBase(request.valorBase());
        
        return PecaResponse.daEntidade(pecaRepository.save(peca));
    }

    @Transactional
    public PecaResponse atualizar(Integer id, PecaRequest request) {
        Peca peca = buscarEntidade(id);
        peca.setNome(request.nome());
        peca.setFabricante(request.fabricante());
        peca.setValorBase(request.valorBase());
        
        return PecaResponse.daEntidade(pecaRepository.save(peca));
    }

    @Transactional
    public void deletar(Integer id) {
        Peca peca = buscarEntidade(id);
        pecaRepository.delete(peca);
    }
}
