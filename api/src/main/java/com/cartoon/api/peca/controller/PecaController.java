package com.cartoon.api.peca.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.peca.dto.PecaFiltro;
import com.cartoon.api.peca.dto.request.PecaRequest;
import com.cartoon.api.peca.dto.response.PecaResponse;
import com.cartoon.api.peca.service.PecaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/pecas")
@AllArgsConstructor
public class PecaController {
    private final PecaService pecaService;

    @PostMapping
    public ResponseEntity<PecaResponse> salvar(
            @Valid @RequestBody PecaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        PecaResponse salva = pecaService.salvar(request, solicitante);
        return ResponseEntity.created(URI.create("/pecas/" + salva.id())).body(salva);
    }

    @GetMapping("/{id}")
    public PecaResponse buscar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        return pecaService.buscar(id, solicitante);
    }

    @GetMapping
    public Page<PecaResponse> listar(
            PecaFiltro pecaFiltro,
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return pecaService.listar(pecaFiltro, pageable, solicitante);
    }

    @PutMapping("/{id}")
    public PecaResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PecaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return pecaService.atualizar(id, request, solicitante);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        pecaService.deletar(id, solicitante);
        return ResponseEntity.noContent().build();
    }
}
