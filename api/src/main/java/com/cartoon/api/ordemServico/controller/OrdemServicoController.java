package com.cartoon.api.ordemServico.controller;


import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("api/ordem-servico/")
@AllArgsConstructor
public class OrdemServicoController {
    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> salvar(@Valid @RequestBody OrdemServicoRequest request) {
        OrdemServicoResponse criada = ordemServicoService.salvar(request);
        return ResponseEntity.created(URI.create("/ordens-servico/" + criada.id())).body(criada);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscar(@PathVariable Integer id) {
        return ordemServicoService.buscar(id);
    }

    @GetMapping
    public Page<OrdemServicoResumo> listar(OrdemServicoFiltro filtro, Pageable pageable) {
        return ordemServicoService.listar(filtro, pageable);
    }

    @PostMapping("/{id}/aceitar")
    public OrdemServicoResponse aceitar(@PathVariable Integer id) {
        return ordemServicoService.aceitar(id);
    }
    @PostMapping("/{id}/iniciar")
    public OrdemServicoResponse iniciar(@PathVariable Integer id) {
        return ordemServicoService.iniciar(id);
    }

    @PostMapping("/{id}/finalizar")
    public OrdemServicoResponse finalizar(@PathVariable Integer id) {
        return ordemServicoService.finalizar(id);
    }
    @PostMapping("/{id}/rejeitar")
    public OrdemServicoResponse rejeitar(@PathVariable Integer id) {
        return ordemServicoService.rejeitar(id);
    }

    @PostMapping("/{id}/itens-peca")
    public OrdemServicoResponse adicionarPeca(@PathVariable Integer id,
                                              @Valid @RequestBody ItemPecaRequest request) {
        return ordemServicoService.adicionarPeca(id, request);
    }

    @DeleteMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse removerPeca(@PathVariable Integer id, @PathVariable Integer itemId) {
        return ordemServicoService.removerPeca(id, itemId);
    }
}
