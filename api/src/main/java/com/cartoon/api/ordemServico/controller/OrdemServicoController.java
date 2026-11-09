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
    public OrdemServicoResponse buscar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.buscar(id, solicitante);
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
    public OrdemServicoResponse aceitar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.aceitar(id, solicitante);
    }

    @RequestMapping(value = "/{id}/iniciar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse iniciar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.iniciar(id, solicitante);
    }

    @RequestMapping(value = "/{id}/finalizar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse finalizar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.finalizar(id, solicitante);
    }

    @RequestMapping(value = "/{id}/rejeitar", method = {RequestMethod.POST, RequestMethod.PATCH})
    public OrdemServicoResponse rejeitar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.rejeitar(id, solicitante);
    }

    @PostMapping("/{id}/itens-peca")
    public OrdemServicoResponse adicionarPeca(
            @PathVariable Integer id,
            @Valid @RequestBody ItemPecaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.adicionarPeca(id, request, solicitante);
    }

    @DeleteMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse removerPeca(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.removerPeca(id, itemId, solicitante);
    }

    @PostMapping("/{id}/itens-servico")
    public OrdemServicoResponse adicionarServico(
            @PathVariable Integer id,
            @Valid @RequestBody ItemServicoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.adicionarServico(id, request, solicitante);
    }

    @DeleteMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse removerServico(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.removerServico(id, itemId, solicitante);
    }

    @PutMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse atualizarValorItemServico(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.atualizarItemServico(id, itemId, request, solicitante);
    }

    @PatchMapping("/{id}/itens-servico/{itemId}")
    public OrdemServicoResponse patchValorItemServico(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.atualizarItemServico(id, itemId, request, solicitante);
    }

    @PutMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse atualizarValorItemPeca(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.atualizarValorItemPeca(id, itemId, request.valorUnitario(), solicitante);
    }

    @PatchMapping("/{id}/itens-peca/{itemId}")
    public OrdemServicoResponse patchValorItemPeca(
            @PathVariable Integer id,
            @PathVariable Integer itemId,
            @Valid @RequestBody AtualizarItemValorRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.atualizarValorItemPeca(id, itemId, request.valorUnitario(), solicitante);
    }

    @PatchMapping("/{id}/desconto")
    public OrdemServicoResponse aplicarDesconto(
            @PathVariable Integer id,
            @Valid @RequestBody DescontoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.aplicarDesconto(id, request, solicitante);
    }

    @PutMapping("/{id}/desconto")
    public OrdemServicoResponse atualizarDesconto(
            @PathVariable Integer id,
            @Valid @RequestBody DescontoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ordemServicoService.aplicarDesconto(id, request, solicitante);
    }
}
