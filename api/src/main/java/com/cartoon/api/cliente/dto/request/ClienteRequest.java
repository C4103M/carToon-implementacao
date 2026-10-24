package com.cartoon.api.cliente.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 dígitos, apenas números")
    private String cpf;

    private String telefone;

    @NotBlank(message = "O endereço é obrigatório")
    private String endereco;

    private Integer oficinaId;

    public void setCpf(String cpf) {
        this.cpf = cpf != null ? cpf.replaceAll("\\D", "") : null;
    }
}
