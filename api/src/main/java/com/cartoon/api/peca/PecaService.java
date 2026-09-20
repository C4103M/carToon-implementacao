package com.cartoon.api.peca;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.models.OrdemServico;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PecaService {
    private final PecaRepository pecaRepository;

    public Peca buscarEntidade(Integer id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peça", id));
    }
}
