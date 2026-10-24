package com.cartoon.api.usuario.dto.request;

import com.cartoon.api.compartilhado.validacao.PossuiConfirmacaoSenha;
import com.cartoon.api.compartilhado.validacao.SenhasCoincidentes;
import com.cartoon.api.usuario.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@SenhasCoincidentes
public record AtualizarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        Role role,

        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String senha,

        String confirmarSenha
) implements PossuiConfirmacaoSenha {

    public AtualizarUsuarioRequest(String nome, String email, String senha, String confirmarSenha) {
        this(nome, email, null, senha, confirmarSenha);
    }
}
