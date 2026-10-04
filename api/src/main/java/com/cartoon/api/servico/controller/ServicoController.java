package com.cartoon.api.servico.controller;

import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.service.ServicoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/servicos")
@AllArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponse> salvar(@Valid @RequestBody ServicoRequest request) {
        ServicoResponse salva = servicoService.salvar(request);
        return ResponseEntity.created(URI.create("/servicos/" + salva.id())).body(salva);
    }

    @GetMapping("/{id}")
    public ServicoResponse buscar(@PathVariable Integer id) {
        return servicoService.buscar(id);
    }

    @GetMapping
    public Page<ServicoResponse> listar(@RequestParam(required = false) String nome, Pageable pageable) {
        return servicoService.listar(nome, pageable);
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ServicoRequest request) {
        return servicoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
