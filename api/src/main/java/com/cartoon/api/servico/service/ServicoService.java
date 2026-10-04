package com.cartoon.api.servico.service;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.servico.dto.mapper.ServicoMapper;
import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repositories.ServicoRepository;
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
        return ServicoMapper.paraServicoResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listar(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return servicoRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable)
                    .map(ServicoMapper::paraServicoResponse);
        }
        return servicoRepository.findAll(pageable).map(ServicoMapper::paraServicoResponse);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponse> listar(Pageable pageable) {
        return listar(null, pageable);
    }

    @Transactional
    public ServicoResponse salvar(ServicoRequest request) {
        Servico servico = ServicoMapper.paraServico(request);
        return ServicoMapper.paraServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Integer id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        ServicoMapper.atualizarEntidade(servico, request);
        return ServicoMapper.paraServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public void deletar(Integer id) {
        Servico servico = buscarEntidade(id);
        servicoRepository.delete(servico);
    }
}
