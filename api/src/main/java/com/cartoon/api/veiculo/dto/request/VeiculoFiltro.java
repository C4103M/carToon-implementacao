package com.cartoon.api.veiculo.dto.request;

public record VeiculoFiltro(
        Integer clienteId,
        String placa,
        String modelo,
        String montadora,
        Boolean ativo
) {}
