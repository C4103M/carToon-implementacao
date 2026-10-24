package com.cartoon.api.cliente.controller;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@AllArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(
            @Valid @RequestBody ClienteRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        ClienteResponse response = clienteService.salvar(request, solicitante);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos(
            @RequestParam(required = false) Boolean ativo,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        if (Boolean.FALSE.equals(ativo)) {
            return ResponseEntity.ok(clienteService.listarInativos(solicitante));
        }
        return ResponseEntity.ok(clienteService.listarTodos(solicitante));
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<ClienteResponse>> listarInativos(
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ResponseEntity.ok(clienteService.listarInativos(solicitante));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        return ResponseEntity.ok(clienteService.buscarPorId(id, solicitante));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        ClienteResponse response = clienteService.atualizar(id, request, solicitante);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> inativar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        clienteService.inativar(id, solicitante);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id:\\d+}/ativar")
    public ResponseEntity<Void> ativar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UsuarioAutenticado solicitante
    ) {
        clienteService.ativar(id, solicitante);
        return ResponseEntity.noContent().build();
    }
}
