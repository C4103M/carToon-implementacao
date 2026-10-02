package com.cartoon.api.veiculo;

import com.cartoon.api.ordemServico.dto.response.OrdemServicoHistoricoResponse;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Endpoints para manutenção de veículos (UC05) e consulta de histórico (UC06)")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @Operation(summary = "Cadastrar veículo", description = "UC05 - Cadastra um novo veículo vinculado a um cliente")
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest request) {
        VeiculoResponse criada = veiculoService.cadastrar(request);
        return ResponseEntity.created(URI.create("/api/veiculos/" + criada.id())).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo", description = "UC05 - Edita os dados cadastrais de um veículo existente")
    public ResponseEntity<VeiculoResponse> atualizar(@PathVariable Integer id,
                                                    @Valid @RequestBody VeiculoAtualizacaoRequest request) {
        VeiculoResponse atualizada = veiculoService.atualizar(id, request);
        return ResponseEntity.ok(atualizada);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID", description = "UC05 - Consulta os detalhes de um veículo pelo ID")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(veiculoService.buscarPorId(id));
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa", description = "UC05 - Consulta os detalhes de um veículo pela placa")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(veiculoService.buscarPorPlaca(placa));
    }

    @GetMapping
    @Operation(summary = "Listar veículos", description = "UC05 - Consulta paginada com filtros opcionais")
    public ResponseEntity<Page<VeiculoResponse>> listar(VeiculoFiltro filtro, Pageable pageable) {
        return ResponseEntity.ok(veiculoService.listar(filtro, pageable));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos por cliente", description = "UC05 - Retorna todos os veículos pertencentes a um cliente")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(veiculoService.listarPorCliente(clienteId));
    }

    @PatchMapping("/{id}/inativar")
    @Operation(summary = "Inativar veículo", description = "UC05 - Inativa o veículo (soft delete) preservando o histórico de ordens")
    public ResponseEntity<Void> inativar(@PathVariable Integer id) {
        veiculoService.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar veículo", description = "UC05 - Reativa um veículo previamente inativado")
    public ResponseEntity<Void> reativar(@PathVariable Integer id) {
        veiculoService.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{placa}/historico")
    @Operation(summary = "Consultar histórico de manutenção pela placa", description = "UC06 - Retorna todas as ordens de serviço do veículo ordenadas da mais recente para a mais antiga")
    public ResponseEntity<HistoricoVeiculoResponse> consultarHistorico(@PathVariable String placa) {
        return ResponseEntity.ok(ordemServicoService.consultarHistoricoPorPlaca(placa));
    }

    @GetMapping("/placa/{placa}/historico")
    @Operation(summary = "Consultar histórico pela placa (rota alternativa)", description = "UC06 - Consulta histórico veicular")
    public ResponseEntity<HistoricoVeiculoResponse> consultarHistoricoAlternativa(@PathVariable String placa) {
        return ResponseEntity.ok(ordemServicoService.consultarHistoricoPorPlaca(placa));
    }

    @GetMapping("/{placa}/historico/paginado")
    @Operation(summary = "Consultar histórico paginado", description = "UC06 - Histórico paginado para veículos com grande volume de ordens")
    public ResponseEntity<Page<OrdemServicoHistoricoResponse>> consultarHistoricoPaginado(@PathVariable String placa,
                                                                                          Pageable pageable) {
        return ResponseEntity.ok(ordemServicoService.consultarHistoricoPaginadoPorPlaca(placa, pageable));
    }
}
