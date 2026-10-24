package com.cartoon.api.servico.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.servico.dto.mapper.ServicoMapper;
import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repository.ServicoRepository;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final UsuarioService usuarioService;

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
    public ServicoResponse buscar(Integer id, UsuarioAutenticado solicitante) {
        Servico servico = buscarEntidade(id);
        validarAcessoOficina(servico, solicitante);
        return ServicoMapper.paraServicoResponse(servico);
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
    public Page<ServicoResponse> listar(String nome, Pageable pageable, UsuarioAutenticado solicitante) {
        if (solicitante == null) {
            return listar(nome, pageable);
        }
        Oficina oficina = obterOficinaDoSolicitante(solicitante);
        if (nome != null && !nome.isBlank()) {
            return servicoRepository.findByOficinaIdAndNomeContainingIgnoreCase(oficina.getId(), nome.trim(), pageable)
                    .map(ServicoMapper::paraServicoResponse);
        }
        return servicoRepository.findByOficinaId(oficina.getId(), pageable)
                .map(ServicoMapper::paraServicoResponse);
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
    public ServicoResponse salvar(ServicoRequest request, UsuarioAutenticado solicitante) {
        Servico servico = ServicoMapper.paraServico(request);
        if (solicitante != null) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            servico.setOficina(oficina);
        }
        return ServicoMapper.paraServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Integer id, ServicoRequest request) {
        Servico servico = buscarEntidade(id);
        ServicoMapper.atualizarEntidade(servico, request);
        return ServicoMapper.paraServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponse atualizar(Integer id, ServicoRequest request, UsuarioAutenticado solicitante) {
        Servico servico = buscarEntidade(id);
        validarAcessoOficina(servico, solicitante);
        ServicoMapper.atualizarEntidade(servico, request);
        return ServicoMapper.paraServicoResponse(servicoRepository.save(servico));
    }

    @Transactional
    public void deletar(Integer id) {
        Servico servico = buscarEntidade(id);
        servicoRepository.delete(servico);
    }

    @Transactional
    public void deletar(Integer id, UsuarioAutenticado solicitante) {
        Servico servico = buscarEntidade(id);
        validarAcessoOficina(servico, solicitante);
        servicoRepository.delete(servico);
    }

    private Oficina obterOficinaDoSolicitante(UsuarioAutenticado solicitante) {
        Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
        Oficina oficina = usuario.getOficina();
        if (oficina == null) {
            throw new AccessDeniedException("Seu usuário não está vinculado a uma oficina.");
        }
        return oficina;
    }

    private void validarAcessoOficina(Servico servico, UsuarioAutenticado solicitante) {
        if (solicitante == null || "SUPERADMIN".equalsIgnoreCase(solicitante.role())) {
            return;
        }
        Oficina oficinaSolicitante = obterOficinaDoSolicitante(solicitante);
        if (servico.getOficina() != null && !servico.getOficina().getId().equals(oficinaSolicitante.getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar ou alterar serviços de outra oficina.");
        }
    }
}
