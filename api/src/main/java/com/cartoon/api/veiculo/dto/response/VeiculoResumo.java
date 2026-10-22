package com.cartoon.api.veiculo.dto.response;

import java.math.BigDecimal;

public record VeiculoResumo(
        Integer id,
        String placa,
        String marca,
        String modelo,
        Integer anoModelo,
        String combustivel,
        String codigoFipe,
        BigDecimal valorFipe,
        String mesReferencia,
        Boolean ativo,
        String clienteNome
) {}
