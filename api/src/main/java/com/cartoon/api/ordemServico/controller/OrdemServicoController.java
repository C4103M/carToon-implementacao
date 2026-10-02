package com.cartoon.api.ordemServico.controller;

import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/ordem-servico")
@AllArgsConstructor
@Tag(name = "Ordens de Serviço", description = "Endpoints para gerenciamento de ordens de serviço e consulta de histórico")
public class OrdemServicoController {
    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> salvar(@Valid @RequestBody OrdemServicoRequest request) {
        OrdemServicoResponse criada = ordemServicoService.salvar(request);
        return ResponseEntity.created(URI.create("/api/ordem-servico/" + criada.id())).body(criada);
    }

    @GetMapping("/{id}")
    public OrdemServicoResponse buscar(@PathVariable Integer id) {
        return ordemServicoService.buscar(id);
    }

    @GetMapping("/{id}/detalhado")
    @Operation(summary = "Detalhar ordem de serviço", description = "UC06 - Retorna todos os detalhes da ordem com peças e serviços")
    public ResponseEntity<OrdemServicoHistoricoResponse> detalhar(@PathVariable Integer id) {
        return ResponseEntity.ok(ordemServicoService.detalharOrdem(id));
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

    @GetMapping("/historico/{placa}")
    @Operation(summary = "Consultar histórico de ordens por placa", description = "UC06 - Retorna histórico de ordens da mais recente para a mais antiga")
    public ResponseEntity<HistoricoVeiculoResponse> consultarHistorico(@PathVariable String placa) {
        return ResponseEntity.ok(ordemServicoService.consultarHistoricoPorPlaca(placa));
    }

    @GetMapping("/historico/{placa}/paginado")
    @Operation(summary = "Consultar histórico de ordens paginado", description = "UC06 - Histórico paginado para veículos com muitas ordens")
    public ResponseEntity<Page<OrdemServicoHistoricoResponse>> consultarHistoricoPaginado(@PathVariable String placa,
                                                                                          Pageable pageable) {
        return ResponseEntity.ok(ordemServicoService.consultarHistoricoPaginadoPorPlaca(placa, pageable));
    }
}
