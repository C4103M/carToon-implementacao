package com.cartoon.api.veiculo.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.veiculo.service.VeiculoService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Endpoints para manutenção de veículos (UC05) e consulta de histórico (UC06)")
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    @Operation(summary = "Cadastrar veículo", description = "UC05 - Cadastra um novo veículo vinculado a um cliente")
    public ResponseEntity<VeiculoResponse> cadastrar(
            @Valid @RequestBody VeiculoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        VeiculoResponse criada = veiculoService.cadastrar(request, solicitante);
        return ResponseEntity.created(URI.create("/api/veiculos/" + criada.id())).body(criada);
    }

    @PutMapping("/{id:\\d+}")
    @Operation(summary = "Atualizar veículo", description = "UC05 - Edita os dados cadastrais de um veículo existente")
    public VeiculoResponse atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody VeiculoAtualizacaoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.atualizar(id, request, solicitante);
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Buscar veículo por ID", description = "UC05 - Consulta os detalhes de um veículo pelo ID")
    public VeiculoResponse buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.buscarPorId(id, solicitante);
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa", description = "UC05 - Consulta os detalhes de um veículo pela placa")
    public VeiculoResponse buscarPorPlaca(
            @PathVariable String placa,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.buscarPorPlaca(placa, solicitante);
    }

    @GetMapping
    @Operation(summary = "Listar veículos", description = "UC05 - Consulta paginada com filtros opcionais")
    public Page<VeiculoResponse> listar(
            VeiculoFiltro filtro,
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.listar(filtro, pageable, solicitante);
    }

    @GetMapping("/cliente/{clienteId:\\d+}")
    @Operation(summary = "Listar veículos por cliente", description = "UC05 - Retorna todos os veículos pertencentes a um cliente")
    public List<VeiculoResponse> listarPorCliente(
            @PathVariable Integer clienteId,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.listarPorCliente(clienteId, solicitante);
    }

    @PatchMapping("/{id:\\d+}/inativar")
    @Operation(summary = "Inativar veículo", description = "UC05 - Inativa o veículo (soft delete) preservando o histórico de ordens")
    public VeiculoResponse inativar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.inativar(id, solicitante);
    }

    @PatchMapping("/{id:\\d+}/reativar")
    @Operation(summary = "Reativar veículo", description = "UC05 - Reativa um veículo previamente inativado")
    public VeiculoResponse reativar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.reativar(id, solicitante);
    }

    @PatchMapping("/{id:\\d+}/atualizar-fipe")
    @Operation(summary = "Atualizar valor FIPE do veículo", description = "UC05 - Atualiza o valor FIPE do veículo com a cotação mais recente")
    public VeiculoResponse atualizarValorFipe(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.atualizarValorFipe(id, solicitante);
    }

    @GetMapping({"/placa/{placa}/historico", "/{placa}/historico"})
    @Operation(summary = "Consultar histórico de manutenção pela placa", description = "UC06 - Retorna todas as ordens de serviço do veículo ordenadas da mais recente para a mais antiga")
    public HistoricoVeiculoResponse consultarHistorico(
            @PathVariable String placa,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.consultarHistorico(placa, solicitante);
    }

    @GetMapping({"/placa/{placa}/historico/paginado", "/{placa}/historico/paginado"})
    @Operation(summary = "Consultar histórico paginado", description = "UC06 - Histórico paginado para veículos com grande volume de ordens")
    public Page<OrdemServicoResumo> consultarHistoricoPaginado(
            @PathVariable String placa,
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return veiculoService.consultarHistoricoPaginado(placa, pageable, solicitante);
    }
}
