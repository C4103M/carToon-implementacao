package com.cartoon.api.cliente;

import com.cartoon.api.cliente.mapper.ClienteMapper;
import com.cartoon.api.cliente.request.ClienteRequest;
import com.cartoon.api.cliente.response.ClienteResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;git pull origin main

@RestController
@RequestMapping("/api/clientes")
@AllArgsConstructor
@PreAuthorize("hasRole('MECANICO')")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody ClienteRequest request) {
        try {
            Cliente cliente = clienteMapper.toEntity(request);
            Cliente clienteSalvo = clienteService.salvar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toResponse(clienteSalvo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequest request) {
        try {
            Cliente clienteExistente = clienteService.buscarEntidade(id);
            clienteMapper.updateEntity(clienteExistente, request);
            
            Cliente clienteSalvo = clienteService.salvar(clienteExistente);
            return ResponseEntity.ok(clienteMapper.toResponse(clienteSalvo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        List<ClienteResponse> clientes = clienteService.listarTodos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Integer id) {
        ClienteResponse cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(cliente);
    }
}