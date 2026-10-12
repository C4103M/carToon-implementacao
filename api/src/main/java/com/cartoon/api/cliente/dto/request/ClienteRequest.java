package com.cartoon.api.cliente.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClienteRequest {
    @NotBlank(message = "O nome é obrigatório")
    private String nome;
    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;
    private String telefone;
    private String endereco;
    @NotNull(message = "A oficina é obrigatória")
    private Integer oficinaId;
}