package com.cartoon.api.usuario.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criarMecanico(MecanicoRequest request, UsuarioAutenticado autenticado) {
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        Oficina oficinaDoAdmin = exigirOficinaDoUsuario(usuarioLogado);
        return cadastrar(request.nome(), request.email(), request.senha(), Role.MECANICO, oficinaDoAdmin);
    }

    @Transactional
    public UsuarioResponse criarAdmin(AdminRequest request) {
        Oficina oficinaDestino = oficinaRepository.findById(request.oficinaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", request.oficinaId()));
        return cadastrar(request.nome(), request.email(), request.senha(), Role.ADMIN, oficinaDestino);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Integer oficinaId, UsuarioAutenticado autenticado) {
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        List<Usuario> usuariosAtivos;
        if (usuarioLogado.getRole() == Role.SUPERADMIN) {
            usuariosAtivos = (oficinaId == null)
                    ? usuarioRepository.findAllByAtivoTrue()
                    : usuarioRepository.findAllByAtivoTrueAndOficinaId(oficinaId);
        } else {
            Integer oficinaDoLogado = exigirOficinaDoUsuario(usuarioLogado).getId();
            usuariosAtivos = usuarioRepository.findAllByAtivoTrueAndOficinaId(oficinaDoLogado).stream()
                    .filter(usuario -> usuario.getRole() != Role.SUPERADMIN)
                    .toList();
        }
        return usuariosAtivos.stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Integer id, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaVer(usuarioAlvo, usuarioLogado);
        return paraResponse(usuarioAlvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarMeusDados(UsuarioAutenticado autenticado) {
        return paraResponse(buscarUsuarioLogado(autenticado));
    }


    @Transactional
    public UsuarioResponse atualizar(Integer id, AtualizarUsuarioRequest request, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaGerenciar(usuarioAlvo, usuarioLogado);
        if (!usuarioAlvo.getAtivo()) {
            throw new ConflitoException("Usuário inativo. Reative-o antes de editar.");
        }
        return aplicarAtualizacao(usuarioAlvo, request);
    }

    @Transactional
    public UsuarioResponse atualizarMeusDados(AtualizarUsuarioRequest request, UsuarioAutenticado autenticado) {
        return aplicarAtualizacao(buscarUsuarioLogado(autenticado), request);
    }

    @Transactional
    public void desativar(Integer id, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaGerenciar(usuarioAlvo, usuarioLogado);
        if (usuarioAlvo.getId().equals(usuarioLogado.getId())) {
            throw new ConflitoException("Você não pode desativar o próprio usuário.");
        }
        usuarioAlvo.setAtivo(false);
    }

    @Transactional
    public void ativar(Integer id, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaGerenciar(usuarioAlvo, usuarioLogado);
        usuarioAlvo.setAtivo(true);
    }

    public Usuario buscarEntidade(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    private Usuario buscarUsuarioLogado(UsuarioAutenticado autenticado) {
        return buscarEntidade(autenticado.id().intValue());
    }

    private void exigirPermissaoParaVer(Usuario usuarioAlvo, Usuario usuarioLogado) {
        if (usuarioLogado.getRole() == Role.SUPERADMIN) {
            return;
        }
        if (usuarioAlvo.getRole() == Role.SUPERADMIN) {
            throw new AccessDeniedException("Você não tem permissão para ver este usuário.");
        }
        if (!pertencemAMesmaOficina(usuarioAlvo, usuarioLogado)) {
            throw new AccessDeniedException("Você só pode ver usuários da sua oficina.");
        }
    }

    private void exigirPermissaoParaGerenciar(Usuario usuarioAlvo, Usuario usuarioLogado) {
        boolean alvoEhOProprioLogado = usuarioAlvo.getId().equals(usuarioLogado.getId());

        if (usuarioLogado.getRole() == Role.SUPERADMIN) {
            if (usuarioAlvo.getRole() == Role.SUPERADMIN && !alvoEhOProprioLogado) {
                throw new AccessDeniedException("Não é permitido gerenciar outro SUPERADMIN.");
            }
            return;
        }

        boolean adminGerenciandoMecanicoOuASiMesmo = usuarioLogado.getRole() == Role.ADMIN
                && pertencemAMesmaOficina(usuarioAlvo, usuarioLogado)
                && (usuarioAlvo.getRole() == Role.MECANICO || alvoEhOProprioLogado);
        if (!adminGerenciandoMecanicoOuASiMesmo) {
            throw new AccessDeniedException("Você não tem permissão para gerenciar este usuário.");
        }
    }

    private boolean pertencemAMesmaOficina(Usuario primeiro, Usuario segundo) {
        return primeiro.getOficina() != null && segundo.getOficina() != null
                && primeiro.getOficina().getId().equals(segundo.getOficina().getId());
    }

    private Oficina exigirOficinaDoUsuario(Usuario usuario) {
        if (usuario.getOficina() == null) {
            throw new AccessDeniedException("Seu usuário não está vinculado a uma oficina.");
        }
        return usuario.getOficina();
    }

    private UsuarioResponse cadastrar(String nome, String email, String senha, Role role, Oficina oficina) {
        if (!oficina.getAtivo()) {
            throw new ConflitoException("Oficina inativa. Reative-a antes de criar usuários.");
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome.trim());
        novoUsuario.setEmail(email.trim());
        novoUsuario.setSenha(passwordEncoder.encode(senha));
        novoUsuario.setRole(role);
        novoUsuario.setOficina(oficina);
        return paraResponse(usuarioRepository.saveAndFlush(novoUsuario));
    }

    private UsuarioResponse aplicarAtualizacao(Usuario usuario, AtualizarUsuarioRequest request) {
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim());
        if (request.senha() != null) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }
        usuarioRepository.flush();
        return paraResponse(usuario);
    }

    private UsuarioResponse paraResponse(Usuario usuario) {
        Integer oficinaId = (usuario.getOficina() == null) ? null : usuario.getOficina().getId();
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                usuario.getRole(), usuario.getAtivo(), oficinaId);
    }
}