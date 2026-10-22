package com.cartoon.api.fipe.dto;

import java.math.BigDecimal;

public record FipeValorDTO(
        String marca,
        String modelo,
        Integer anoModelo,
        String combustivel,
        String siglaCombustivel,
        String codigoFipe,
        BigDecimal valorFipe,
        String mesReferencia
) {}
