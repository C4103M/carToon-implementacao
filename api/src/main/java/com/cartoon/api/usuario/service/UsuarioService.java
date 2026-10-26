package com.cartoon.api.usuario.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.repository.OficinaRepository;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final List<StatusServico> STATUS_EM_ABERTO = List.of(
            StatusServico.PENDENTE,
            StatusServico.ACEITO,
            StatusServico.EM_ANDAMENTO
    );

    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
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
        return listar(oficinaId, null, autenticado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Integer oficinaId, Boolean ativo, UsuarioAutenticado autenticado) {
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        List<Usuario> usuarios;
        if (usuarioLogado.getRole() == Role.SUPERADMIN) {
            if (oficinaId == null) {
                usuarios = (ativo == null)
                        ? usuarioRepository.findAll()
                        : usuarioRepository.findAllByAtivo(ativo);
            } else {
                usuarios = (ativo == null)
                        ? usuarioRepository.findAllByOficinaId(oficinaId)
                        : usuarioRepository.findAllByAtivoAndOficinaId(ativo, oficinaId);
            }
        } else {
            Integer oficinaDoLogado = exigirOficinaDoUsuario(usuarioLogado).getId();
            List<Usuario> listaBase = (ativo == null)
                    ? usuarioRepository.findAllByOficinaId(oficinaDoLogado)
                    : usuarioRepository.findAllByAtivoAndOficinaId(ativo, oficinaDoLogado);
            usuarios = listaBase.stream()
                    .filter(usuario -> usuario.getRole() != Role.SUPERADMIN)
                    .toList();
        }
        return usuarios.stream().map(this::paraResponse).toList();
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
        if (request.role() != null && request.role() != usuarioAlvo.getRole()) {
            if (usuarioLogado.getRole() != Role.SUPERADMIN && request.role() == Role.SUPERADMIN) {
                throw new AccessDeniedException("Apenas SUPERADMIN pode atribuir o perfil SUPERADMIN.");
            }
            if (usuarioAlvo.getId().equals(usuarioLogado.getId()) && usuarioAlvo.getRole() == Role.ADMIN && request.role() == Role.MECANICO) {
                throw new ConflitoException("Você não pode rebaixar o seu próprio perfil de administrador.");
            }
            usuarioAlvo.setRole(request.role());
        }
        if (usuarioAlvo.getId().equals(usuarioLogado.getId())) {
            validarSenhaAtualSeNecessario(usuarioAlvo, request);
        }
        return aplicarAtualizacao(usuarioAlvo, request);
    }

    @Transactional
    public UsuarioResponse atualizarMeusDados(AtualizarUsuarioRequest request, UsuarioAutenticado autenticado) {
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        validarSenhaAtualSeNecessario(usuarioLogado, request);
        return aplicarAtualizacao(usuarioLogado, request);
    }

    private void validarSenhaAtualSeNecessario(Usuario usuario, AtualizarUsuarioRequest request) {
        boolean alterandoSenha = request.senha() != null && !request.senha().isBlank();
        boolean alterandoEmail = request.email() != null && !usuario.getEmail().equalsIgnoreCase(request.email().trim());
        if (alterandoSenha || alterandoEmail) {
            if (request.senhaAtual() == null || !passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
                throw new RegraDeNegocioException("A senha atual é obrigatória e deve estar correta para alterar e-mail ou senha.");
            }
        }
    }

    @Transactional
    public void desativar(Integer id, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaGerenciar(usuarioAlvo, usuarioLogado);
        if (usuarioAlvo.getId().equals(usuarioLogado.getId())) {
            throw new ConflitoException("Você não pode desativar o próprio usuário.");
        }
        if (ordemServicoRepository.existsByMecanicoIdAndStatusServicoIn(id, STATUS_EM_ABERTO)) {
            throw new ConflitoException(
                    "Não é possível desativar: o mecânico possui ordens de serviço pendentes ou em andamento.");
        }
        usuarioAlvo.setAtivo(false);
    }

    @Transactional
    public void ativar(Integer id, UsuarioAutenticado autenticado) {
        Usuario usuarioAlvo = buscarEntidade(id);
        Usuario usuarioLogado = buscarUsuarioLogado(autenticado);
        exigirPermissaoParaGerenciar(usuarioAlvo, usuarioLogado);
        if (usuarioAlvo.getOficina() != null && !Boolean.TRUE.equals(usuarioAlvo.getOficina().getAtivo())) {
            throw new ConflitoException("Não é possível reativar usuário de uma oficina inativa.");
        }
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
        novoUsuario.setEmail(email.trim().toLowerCase());
        novoUsuario.setSenha(passwordEncoder.encode(senha));
        novoUsuario.setRole(role);
        novoUsuario.setOficina(oficina);
        novoUsuario.setAtivo(true);
        return paraResponse(usuarioRepository.saveAndFlush(novoUsuario));
    }

    private UsuarioResponse aplicarAtualizacao(Usuario usuario, AtualizarUsuarioRequest request) {
        usuario.setNome(request.nome().trim());
        usuario.setEmail(request.email().trim().toLowerCase());
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