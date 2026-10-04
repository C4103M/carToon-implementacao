package com.cartoon.api.oficina.controller;


import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.service.OficinaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.cartoon.api.auth.UsuarioAutenticado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/oficina")
@RequiredArgsConstructor
public class OficinaController {

    private final OficinaService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OficinaResponse criar(@Valid @RequestBody OficinaRequest request){
        return service.criar(request);
    }

    @GetMapping
    public List<OficinaResponse> listar(){
        return service.listar();
    }

    @GetMapping("/{id}")
    public OficinaResponse buscarPorId(@PathVariable Integer id){
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public OficinaResponse atualizar(@PathVariable Integer id,
                                     @Valid @RequestBody OficinaRequest request,
                                     @AuthenticationPrincipal UsuarioAutenticado solicitante){
        return service.atualizar(id, request, solicitante);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<Void> desativar(@PathVariable Integer id){
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Integer id){
        service.ativar(id);
        return ResponseEntity.noContent().build();
    }
}
