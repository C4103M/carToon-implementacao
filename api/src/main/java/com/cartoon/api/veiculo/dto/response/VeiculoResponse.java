package com.cartoon.api.veiculo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        String clienteCpf,
        String mensagem
) {
    public VeiculoResponse(Integer id, String placa, String modelo, Integer ano, String montadora, Double valorFipe, Boolean ativo, Integer clienteId, String clienteNome, String clienteCpf) {
        this(id, placa, modelo, ano, montadora, valorFipe, ativo, clienteId, clienteNome, clienteCpf, null);
    }
}
