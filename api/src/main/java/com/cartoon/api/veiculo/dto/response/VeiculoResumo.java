package com.cartoon.api.veiculo.dto.response;

public record VeiculoResumo(
        Integer id,
        String placa,
        String modelo,
        String montadora,
        Integer ano,
        Double valorFipe,
        Boolean ativo,
        String clienteNome
) {}
