package com.cartoon.api.oficina.dto.response;

public record OficinaResponse (Integer id,
                               String nome,
                               String endereco,
                               String telefone, Boolean ativo){
}
