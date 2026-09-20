package com.cartoon.api.oficina;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OficinaService {
    private final OficinaRepository oficinaRepository;

    public Oficina buscarEntidade(Integer id) {
        return oficinaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Oficina", id));
    }
}
