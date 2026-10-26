package com.cartoon.api.oficina.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.repository.OficinaRepository;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.usuario.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

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
    private final UsuarioService usuarioService;
    private final com.cartoon.api.usuario.repository.UsuarioRepository usuarioRepository;

    public OficinaService(
            OficinaRepository oficinaRepository,
            OrdemServicoRepository ordemServicoRepository,
            OficinaMapper mapper,
            UsuarioService usuarioService
    ) {
        this(oficinaRepository, ordemServicoRepository, mapper, usuarioService, null);
    }

    @Transactional
    public OficinaResponse criar(OficinaRequest request) {
        Oficina oficina = mapper.toEntity(request);
        return mapper.toResponse(oficinaRepository.saveAndFlush(oficina));
    }

    @Transactional(readOnly = true)
    public List<OficinaResponse> listar(Boolean ativo, UsuarioAutenticado solicitante) {
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
            Oficina oficinaUsuario = usuario.getOficina();
            if (oficinaUsuario == null) {
                return List.of();
            }
            if (ativo != null && !ativo.equals(oficinaUsuario.getAtivo())) {
                return List.of();
            }
            return List.of(mapper.toResponse(oficinaUsuario));
        }

        List<Oficina> oficinas = (ativo == null)
                ? oficinaRepository.findAll()
                : oficinaRepository.findAllByAtivo(ativo);
        return oficinas.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OficinaResponse buscarPorId(Integer id, UsuarioAutenticado solicitante) {
        Oficina oficina = buscarEntidade(id);
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
            Oficina oficinaUsuario = usuario.getOficina();
            if (oficinaUsuario == null || !oficinaUsuario.getId().equals(id)) {
                throw new AccessDeniedException("Você só pode visualizar os dados da oficina à qual pertence.");
            }
        }
        return mapper.toResponse(oficina);
    }

    @Transactional
    public OficinaResponse atualizar(Integer id, OficinaRequest request, UsuarioAutenticado solicitante) {
        Oficina oficina = buscarEntidade(id);
        validarPermissaoDeEdicao(oficina, solicitante);
        if (!oficina.getAtivo()) {
            throw new ConflitoException("Oficina inativa. Reative-a antes de editar.");
        }
        mapper.updateEntity(oficina, request);
        oficinaRepository.flush();
        return mapper.toResponse(oficina);
    }

    private void validarPermissaoDeEdicao(Oficina oficina, UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.isSuperAdmin()) {
            return;
        }
        Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
        Oficina oficinaUsuario = usuario.getOficina();
        if (oficinaUsuario == null || !oficinaUsuario.getId().equals(oficina.getId())) {
            throw new AccessDeniedException("Você só pode editar a oficina à qual pertence.");
        }
    }

    @Transactional
    public void desativar(Integer id) {
        Oficina oficina = buscarEntidade(id);
        if (ordemServicoRepository.existsByOficinaIdAndStatusServicoIn(id, STATUS_EM_ABERTO)) {
            throw new ConflitoException(
                    "Não é possível desativar: a oficina possui ordens de serviço em aberto.");
        }
        oficina.setAtivo(false);
        if (usuarioRepository != null) {
            usuarioRepository.findAllByOficinaId(id).forEach(u -> u.setAtivo(false));
        }
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
