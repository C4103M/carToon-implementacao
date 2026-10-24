package com.cartoon.api.oficina.dto.response;

import java.math.BigDecimal;

public record OficinaResponse (
        Integer id,
        String nome,
        String endereco,
        String telefone,
        String email,
        BigDecimal valorHora,
        BigDecimal taxaRisco,
        Boolean ativo
) {
    public OficinaResponse(Integer id, String nome, String endereco, String telefone, String email, Boolean ativo) {
        this(id, nome, endereco, telefone, email, null, null, ativo);
    }
}
