package com.cartoon.api.oficina.service;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OficinaService {
    private final OficinaRepository oficinaRepository;
    private final OficinaMapper mapper;

    @Transactional
    public OficinaResponse criar(OficinaRequest request) {
        Oficina oficina = mapper.toEntity(request);
        return mapper.toResponse(oficinaRepository.save(oficina));
    }

    @Transactional(readOnly = true)
    public List<OficinaResponse> listar(){
        return oficinaRepository.findAllByAtivoTrue().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OficinaResponse buscarPorId(Integer id){
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public OficinaResponse atualizar(Integer id, OficinaRequest request) {
        Oficina oficina = buscarEntidade(id);
        mapper.updateEntity(oficina, request);
        return mapper.toResponse(oficina);
    }

    @Transactional
    public void desativar(Integer id){
        buscarEntidade(id).setAtivo(false);
    }

    @Transactional
    public void ativar(Integer id){
        buscarEntidade(id).setAtivo(true);
    }

    public Oficina buscarEntidade(Integer id) {
        return oficinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", id));
    }
}
