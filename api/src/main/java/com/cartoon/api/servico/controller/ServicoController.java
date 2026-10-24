package com.cartoon.api.servico.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.service.ServicoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/servicos")
@AllArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponse> salvar(
            @Valid @RequestBody ServicoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        ServicoResponse salva = servicoService.salvar(request, solicitante);
        return ResponseEntity.created(URI.create("/servicos/" + salva.id())).body(salva);
    }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        return servicoService.buscar(id, solicitante);
    }

    @GetMapping
    public Page<ServicoResponse> listar(
            @RequestParam(required = false) String nome,
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return servicoService.listar(nome, pageable, solicitante);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ServicoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return servicoService.atualizar(id, request, solicitante);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado solicitante) {
        servicoService.deletar(id, solicitante);
        return ResponseEntity.noContent().build();
    }
}
