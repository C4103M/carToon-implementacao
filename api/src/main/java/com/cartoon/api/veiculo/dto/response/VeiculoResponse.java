package com.cartoon.api.veiculo.dto.response;

public record VeiculoResponse(
        Integer id,
        String placa,
        String modelo,
        Integer ano,
        String montadora,
        Double valorFipe,
        Boolean ativo,
        Integer clienteId,
        String clienteNome,
        String clienteCpf
) {}
