package com.cartoon.api.peca.controller;

import com.cartoon.api.peca.dto.PecaFiltro;
import com.cartoon.api.peca.dto.request.PecaRequest;
import com.cartoon.api.peca.dto.response.PecaResponse;
import com.cartoon.api.peca.service.PecaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/pecas")
@AllArgsConstructor
public class PecaController {
    private final PecaService pecaService;

    @PostMapping
    public ResponseEntity<PecaResponse> salvar(@Valid @RequestBody PecaRequest request) {
        PecaResponse salva = pecaService.salvar(request);
        return ResponseEntity.created(URI.create("/pecas/" + salva.id())).body(salva);
    }

    @GetMapping("/{id}")
    public PecaResponse buscar(@PathVariable Integer id) {
        return pecaService.buscar(id);
    }

    @GetMapping
    public Page<PecaResponse> listar(PecaFiltro pecaFiltro, Pageable pageable) {
        return pecaService.listar(pecaFiltro, pageable);
    }

    @PutMapping("/{id}")
    public PecaResponse atualizar(@PathVariable Integer id, @Valid @RequestBody PecaRequest request) {
        return pecaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        pecaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
