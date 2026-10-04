package com.cartoon.api.usuario.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminRequest(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") String senha,
        @NotNull(message = "A oficina é obrigatória") Integer oficina
) {}
