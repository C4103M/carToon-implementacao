package com.cartoon.api.ordemServico.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.DescontoRequest;
import com.cartoon.api.ordemServico.dto.request.AtualizarItemValorRequest;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/ordem-servico")
@AllArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Endpoints para gerenciamento de ordens de serviço")
public class OrdemServicoController {
    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> salvar(
            @Valid @RequestBody OrdemServicoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        OrdemServicoResponse criada = ordemServicoService.salvar(request, solicitante);
        return ResponseEntity.created(URI.create("/ordem-servico/" + criada.id())).body(criada);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscar(@PathVariable Integer id) {
        return ordemServicoService.buscar(id);
    }

    @GetMapping
    public Page<OrdemServicoResumo> listar(
            OrdemServicoFiltro filtro,
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.listar(filtro, pageable, solicitante);
    }

    @RequestMapping(value = "/{id}/aceitar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse aceitar(@PathVariable Integer id) {
        return ordemServicoService.aceitar(id);
    }

    @RequestMapping(value = "/{id}/iniciar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse iniciar(@PathVariable Integer id) {
        return ordemServicoService.iniciar(id);
    }

    @RequestMapping(value = "/{id}/finalizar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse finalizar(@PathVariable Integer id) {
        return ordemServicoService.finalizar(id);
    }

    @RequestMapping(value = "/{id}/rejeitar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse rejeitar(@PathVariable Integer id) {
        return ordemServicoService.rejeitar(id);
    }

    @PostMapping("/{id}/itens-peca")
    public OrdemServicoResponse adicionarPeca(
            @PathVariable Integer id,
            @Valid @RequestBody ItemPecaRequest request
    ) {
        return ordemServicoService.adicionarPeca(id, request);
    }

    @DeleteMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse removerPeca(@PathVariable Integer id, @PathVariable Integer itemId) {
        return ordemServicoService.removerPeca(id, itemId);
    }

    @PostMapping("/{id}/itens-servico")
    public OrdemServicoResponse adicionarServico(
            @PathVariable Integer id,
            @Valid @RequestBody ItemServicoRequest request
    ) {
        return ordemServicoService.adicionarServico(id, request);
    }

    @DeleteMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse removerServico(@PathVariable Integer id, @PathVariable Integer itemId) {
        return ordemServicoService.removerServico(id, itemId);
    }

    @PutMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse atualizarValorItemServico(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request
    ) {
        return ordemServicoService.atualizarItemServico(id, itemId, request);
    }

    @PatchMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse patchValorItemServico(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request
    ) {
        return ordemServicoService.atualizarItemServico(id, itemId, request);
    }

    @PutMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse atualizarValorItemPeca(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request
    ) {
        return ordemServicoService.atualizarValorItemPeca(id, itemId, request.valorUnitario());
    }

    @PatchMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse patchValorItemPeca(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request
    ) {
        return ordemServicoService.atualizarValorItemPeca(id, itemId, request.valorUnitario());
    }

    @PatchMapping("/{id}/desconto")
    public OrdemServicoResponse aplicarDesconto(@PathVariable Integer id, @Valid @RequestBody DescontoRequest request) {
        return ordemServicoService.aplicarDesconto(id, request);
    }

    @PutMapping("/{id}/desconto")
    public OrdemServicoResponse atualizarDesconto(@PathVariable Integer id, @Valid @RequestBody DescontoRequest request) {
        return ordemServicoService.aplicarDesconto(id, request);
    }
}
