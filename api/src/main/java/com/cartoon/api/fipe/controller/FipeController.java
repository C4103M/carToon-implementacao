package com.cartoon.api.fipe.controller;

import com.cartoon.api.fipe.dto.FipeItemDTO;
import com.cartoon.api.fipe.dto.FipeValorDTO;
import com.cartoon.api.fipe.dto.TipoVeiculo;
import com.cartoon.api.fipe.service.FipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fipe/{tipo}")
@RequiredArgsConstructor
@Tag(name = "FIPE", description = "Endpoints para consulta de marcas, modelos, anos e valores na tabela FIPE")
public class FipeController {

    private final FipeService fipeService;

    @GetMapping("/marcas")
    @Operation(summary = "Listar marcas por tipo de veículo", description = "Retorna as marcas disponíveis na FIPE para o tipo informado (carros, motos, caminhoes)")
    public List<FipeItemDTO> listarMarcas(@PathVariable String tipo) {
        return fipeService.listarMarcas(TipoVeiculo.from(tipo));
    }

    @GetMapping("/marcas/{marcaId}/modelos")
    @Operation(summary = "Listar modelos por marca", description = "Retorna os modelos disponíveis na FIPE para a marca informada")
    public List<FipeItemDTO> listarModelos(@PathVariable String tipo,
                                           @PathVariable String marcaId) {
        return fipeService.listarModelos(TipoVeiculo.from(tipo), marcaId);
    }

    @GetMapping("/marcas/{marcaId}/modelos/{modeloId}/anos")
    @Operation(summary = "Listar anos por modelo", description = "Retorna os anos/combustíveis disponíveis na FIPE para o modelo informado")
    public List<FipeItemDTO> listarAnos(@PathVariable String tipo,
                                        @PathVariable String marcaId,
                                        @PathVariable String modeloId) {
        return fipeService.listarAnos(TipoVeiculo.from(tipo), marcaId, modeloId);
    }

    @GetMapping("/marcas/{marcaId}/modelos/{modeloId}/anos/{anoId}")
    @Operation(summary = "Consultar valor FIPE do veículo", description = "Retorna os dados detalhados e o valor de mercado segundo a tabela FIPE")
    public FipeValorDTO consultarValor(@PathVariable String tipo,
                                       @PathVariable String marcaId,
                                       @PathVariable String modeloId,
                                       @PathVariable String anoId) {
        return fipeService.consultarValor(TipoVeiculo.from(tipo), marcaId, modeloId, anoId);
    }
}
