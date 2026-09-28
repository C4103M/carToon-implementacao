package com.cartoon.api.auth;

import com.cartoon.api.usuario.Role;

public record CadastroDTO(String nome, String email, String password, Role role) {
}
