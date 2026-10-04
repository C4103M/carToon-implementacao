package com.cartoon.api.usuario.dto.response;

import com.cartoon.api.usuario.model.Role;

public record UsuarioResponse(Integer id, String nome, String email, Role role, Integer oficina) {}