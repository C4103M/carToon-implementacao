package com.cartoon.api.servico.dto.mapper;

import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;

import java.math.BigDecimal;

public final class ServicoMapper {

    private ServicoMapper() {}

    public static Servico paraServico(ServicoRequest request) {
        Servico servico = new Servico();
        servico.setNome(request.nome());
        servico.setTempo(request.tempo());
        servico.setDescricao(request.descricao());
        if (request.pesoServico() != null) {
            servico.setPesoServicoPadrao(request.pesoServico());
        }
        return servico;
    }

    public static ServicoResponse paraServicoResponse(Servico servico) {
        BigDecimal peso = servico.getPesoServicoPadrao() != null ? servico.getPesoServicoPadrao() : new BigDecimal("0.5");
        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getTempo(),
                servico.getQuantidadeHoras(),
                servico.getDescricao(),
                peso,
                servico.getOficina() != null ? servico.getOficina().getId() : null
        );
    }

    public static void atualizarEntidade(Servico servico, ServicoRequest request) {
        servico.setNome(request.nome());
        servico.setTempo(request.tempo());
        servico.setDescricao(request.descricao());
        if (request.pesoServico() != null) {
            servico.setPesoServicoPadrao(request.pesoServico());
        }
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
