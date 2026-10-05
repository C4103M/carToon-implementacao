package com.cartoon.api.usuario.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import com.cartoon.api.usuario.dto.response.UsuarioResponse;
import com.cartoon.api.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping("/mecanico")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criarMecanico(@Valid @RequestBody MecanicoRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.criarMecanico(request, autenticado);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criarAdmin(@Valid @RequestBody AdminRequest request) {
        return service.criarAdmin(request);
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Integer oficinaId,
                                        @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.listar(oficinaId, autenticado);
    }

    @GetMapping("/me")
    public UsuarioResponse meusDados(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.buscarMeusDados(autenticado);
    }

    @PutMapping("/me")
    public UsuarioResponse atualizarMeusDados(@Valid @RequestBody AtualizarUsuarioRequest request,
                                              @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.atualizarMeusDados(request, autenticado);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Integer id,
                                       @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.buscarPorId(id, autenticado);
    }

    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Integer id,
                                     @Valid @RequestBody AtualizarUsuarioRequest request,
                                     @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return service.atualizar(id, request, autenticado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id,
                          @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        service.desativar(id, autenticado);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Integer id,
                       @AuthenticationPrincipal UsuarioAutenticado autenticado) {
        service.ativar(id, autenticado);
    }
}