package com.cartoon.api.cliente.dto.mapper;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.oficina.Oficina;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setTelefone(request.getTelefone());
        Oficina oficina = new Oficina();
        cliente.setEndereco(request.getEndereco());
        oficina.setId(request.getOficinaId());
        cliente.setOficina(oficina);
        return cliente;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        ClienteResponse response = new ClienteResponse();
        response.setId(cliente.getId());
        response.setNome(cliente.getNome());
        response.setCpf(cliente.getCpf());
        response.setTelefone(cliente.getTelefone());
        response.setEndereco(cliente.getEndereco());
        if (cliente.getOficina() != null) {
            response.setOficinaId(cliente.getOficina().getId());
        }
        return response;
    }

    public void updateEntity(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.getNome());
        cliente.setCpf(request.getCpf());
        cliente.setTelefone(request.getTelefone());
        cliente.setEndereco(request.getEndereco());
        Oficina oficina = new Oficina();
        oficina.setId(request.getOficinaId());
        cliente.setOficina(oficina);
    }
}