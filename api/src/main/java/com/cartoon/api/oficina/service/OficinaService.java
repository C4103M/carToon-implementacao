package com.cartoon.api.oficina.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OficinaService {

    private static final List<StatusServico> STATUS_EM_ABERTO = List.of(
            StatusServico.PENDENTE,
            StatusServico.ACEITO,
            StatusServico.EM_ANDAMENTO
    );

    private final OficinaRepository oficinaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OficinaMapper mapper;

    @Transactional
    public OficinaResponse criar(OficinaRequest request) {
        Oficina oficina = mapper.toEntity(request);
        return mapper.toResponse(oficinaRepository.saveAndFlush(oficina));
    }

    @Transactional(readOnly = true)
    public List<OficinaResponse> listar() {
        return oficinaRepository.findAllByAtivoTrue().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OficinaResponse buscarPorId(Integer id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public OficinaResponse atualizar(Integer id, OficinaRequest request) {
        Oficina oficina = buscarEntidade(id);
        if (!oficina.getAtivo()) {
            throw new ConflitoException("Oficina inativa. Reative-a antes de editar.");
        }
        mapper.updateEntity(oficina, request);
        oficinaRepository.flush();
        return mapper.toResponse(oficina);
    }

    @Transactional
    public void desativar(Integer id) {
        Oficina oficina = buscarEntidade(id);
        if (ordemServicoRepository.existsByOficinaIdAndStatusServicoIn(id, STATUS_EM_ABERTO)) {
            throw new ConflitoException(
                    "Não é possível desativar: a oficina possui ordens de serviço em aberto.");
        }
        oficina.setAtivo(false);
    }

    @Transactional
    public void ativar(Integer id) {
        buscarEntidade(id).setAtivo(true);
    }

    public Oficina buscarEntidade(Integer id) {
        return oficinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", id));
    }
}