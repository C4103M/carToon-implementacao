package com.cartoon.api.servico;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional(readOnly = true)
    public Servico buscarEntidade(Integer id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscar(Integer id) {
        return ServicoResponse.daEntidade(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listar(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return servicoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable).map(ServicoResponse::daEntidade);
        }
        return servicoRepository.findAll(pageable).map(ServicoResponse::daEntidade);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listar(Pageable pageable) {
        return listar(null, pageable);
    }

    @Transactional
    public ServicoResponse salvar(ServicoRequest request) {
        Servico servico = new Servico();
        servico.setNome(request.nome());
        servico.setValorBase(request.valorBase());
        servico.setDescricao(request.descricao());
        servico.setTempoEstimado(request.tempoEstimado());
        
        return ServicoResponse.daEntidade(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Integer id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        servico.setNome(request.nome());
        servico.setValorBase(request.valorBase());
        servico.setDescricao(request.descricao());
        servico.setTempoEstimado(request.tempoEstimado());
        
        return ServicoResponse.daEntidade(servicoRepository.save(servico));
    }

    @Transactional
    public void deletar(Integer id) {
        Servico servico = buscarEntidade(id);
        servicoRepository.delete(servico);
    }
}
