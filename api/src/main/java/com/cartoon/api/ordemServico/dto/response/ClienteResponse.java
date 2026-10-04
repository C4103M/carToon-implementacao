package com.cartoon.api.cliente.response;

import lombok.Data;

@Data
public class ClienteResponse {
    private Integer id;
    private String nome;
    private String cpf;
    private String telefone;
    private String endereco;
    private boolean ativo;
}