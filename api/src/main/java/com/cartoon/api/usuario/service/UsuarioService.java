package com.cartoon.api.usuario.service;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.model.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public Usuario buscarEntidade(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }
}
