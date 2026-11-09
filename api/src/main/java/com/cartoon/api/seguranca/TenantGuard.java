package com.cartoon.api.seguranca;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantGuard {

    private final UsuarioRepository usuarioRepository;

    public Oficina obterOficinaDoSolicitante(UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.id() == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        Usuario usuario = usuarioRepository.findById(solicitante.id().intValue())
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado."));
        Oficina oficina = usuario.getOficina();
        if (oficina == null) {
            throw new AccessDeniedException("Seu usuário não está vinculado a uma oficina.");
        }
        return oficina;
    }

    public void validarAcessoOficina(Oficina oficinaRecurso, UsuarioAutenticado solicitante, String mensagemErro) {
        if (solicitante == null || solicitante.isSuperAdmin()) {
            return;
        }
        Oficina oficinaSolicitante = obterOficinaDoSolicitante(solicitante);
        if (oficinaRecurso != null && !oficinaRecurso.getId().equals(oficinaSolicitante.getId())) {
            throw new AccessDeniedException(mensagemErro);
        }
    }
}
