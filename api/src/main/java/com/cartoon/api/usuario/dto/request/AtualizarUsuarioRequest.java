package com.cartoon.api.usuario.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O e-mail é obrigatório") @Email(message = "E-mail inválido") String email,
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") String senha
) {}