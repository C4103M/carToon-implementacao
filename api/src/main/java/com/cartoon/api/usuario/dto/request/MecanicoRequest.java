package com.cartoon.api.usuario.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MecanicoRequest(
        @NotBlank(message = "o nome é obrigatório") String nome,
        @NotBlank(message = "o email é obrigatório") @Email(message = "E-mail invalido") String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no minimo 8 caracteres") String senha
) {
}
