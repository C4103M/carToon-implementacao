package com.cartoon.api.servico.dto.mapper;

import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;

public final class ServicoMapper {

    private ServicoMapper() {}

    public static Servico paraServico(ServicoRequest request) {
        Servico servico = new Servico();
        servico.setNome(request.nome());
        servico.setValorBase(request.valorBase());
        servico.setDescricao(request.descricao());
        servico.setTempoEstimado(request.tempoEstimado());
        return servico;
    }

    public static ServicoResponse paraServicoResponse(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getValorBase(),
                servico.getDescricao(),
                servico.getTempoEstimado()
        );
    }

    public static void atualizarEntidade(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome());
        servico.setValorBase(request.valorBase());
        servico.setDescricao(request.descricao());
        servico.setTempoEstimado(request.tempoEstimado());
    }

    public static ServicoResponse toResponse(Servico servico) {
        return paraServicoResponse(servico);
    }

    public static Servico toEntity(ServicoRequest request) {
        return paraServico(request);
    }

    public static void updateEntity(Servico servico, ServicoRequest request) {
        atualizarEntidade(servico, request);
    }
}
