package com.cartoon.api.cliente.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteRequest {
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "O telefone é obrigatório")
    private String telefone;

    @NotBlank(message = "O endereço é obrigatório")
    private String endereco;

    @jakarta.validation.constraints.NotNull(message = "A oficina é obrigatória")
    private Integer oficinaId;
}