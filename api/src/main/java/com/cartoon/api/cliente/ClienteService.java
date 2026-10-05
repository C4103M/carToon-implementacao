package com.cartoon.api.cliente;

import com.cartoon.api.cliente.mapper.ClienteMapper;
import com.cartoon.api.cliente.response.ClienteResponse;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
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
        Cliente cliente = buscarEntidade(id);
        return clienteMapper.toResponse(cliente);
    }

    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Cliente salvar(Cliente cliente) {
        if (cliente.getId() == null && clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new IllegalArgumentException("Cliente já cadastrado no sistema.");
        }
        return clienteRepository.save(cliente);
    }
}