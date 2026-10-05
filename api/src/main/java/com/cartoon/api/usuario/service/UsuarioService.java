package com.cartoon.api.usuario.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final OficinaRepository oficinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse criarMecanico(MecanicoRequest request, UsuarioAutenticado solicitante) {
        Oficina oficina = buscarEntidade(solicitante.id().intValue()).getOficina();
        if (oficina == null) {
            throw new AccessDeniedException("Seu usuário não está vinculado a uma oficina.");
        }
        return salvar(request.nome(), request.email(), request.senha(), Role.MECANICO, oficina);
    }

    @Transactional
    public UsuarioResponse criarAdmin(AdminRequest request) {
        Oficina oficina = oficinaRepository.findById(request.oficinaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", request.oficinaId()));
        return salvar(request.nome(),request.email(), request.senha(), Role.ADMIN, oficina);
    }

    @Transactional
    public Usuario buscarEntidade(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }



    private UsuarioResponse salvar(String nome,String email, String senha, Role role, Oficina oficina){
        if (!oficina.getAtivo()){
            throw new ConflitoException("Oficina inativa.Reative-a antes de criar usuários");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim());
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setOficina(oficina);

        Usuario salvo = usuarioRepository.saveAndFlush(usuario);
        return new UsuarioResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getRole(),
                oficina.getId());
    }
}
