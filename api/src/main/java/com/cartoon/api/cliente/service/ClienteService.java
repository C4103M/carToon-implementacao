package com.cartoon.api.cliente.service;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.cliente.dto.mapper.ClienteMapper;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public Cliente buscarEntidade(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public ClienteResponse buscarPorId(Integer id) {
        return clienteMapper.toResponse(buscarEntidade(id));
    }

    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse salvar(ClienteRequest request) {
        if (clienteRepository.existsByCpf(request.getCpf())) {
            throw new ConflitoException("Cliente já cadastrado com este CPF.");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(cliente);
    }

    public ClienteResponse atualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarEntidade(id);

        if (!cliente.getCpf().equals(request.getCpf()) && clienteRepository.existsByCpf(request.getCpf())) {
            throw new ConflitoException("Cliente já cadastrado com este CPF.");
        }

        clienteMapper.updateEntity(cliente, request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(cliente);
    }

    public void excluir(Integer id) {
        Cliente cliente = buscarEntidade(id);
        clienteRepository.delete(cliente);
    }
}