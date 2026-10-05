package com.cartoon.api.usuario.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping("/mecanico")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criarMecanico(@Valid @RequestBody MecanicoRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado solicitante){
        return service.criarMecanico(request,solicitante);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criarAdmin(@Valid @RequestBody AdminRequest request){
        return service.criarAdmin(request);
    }
}
