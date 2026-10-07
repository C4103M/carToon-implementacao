package com.cartoon.api.peca.dto.mapper;

import com.cartoon.api.peca.dto.request.PecaRequest;
import com.cartoon.api.peca.dto.response.PecaResponse;
import com.cartoon.api.peca.models.Peca;

public final class PecaMapper {

    private PecaMapper() {}

    public static Peca paraPeca(PecaRequest request) {
        Peca peca = new Peca();
        peca.setNome(request.nome());
        peca.setFabricante(request.fabricante());
        peca.setValorBase(request.valorBase());
        return peca;
    }

    public static PecaResponse paraPecaResponse(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getFabricante(),
                peca.getValorBase()
        );
    }

    public static void atualizarEntidade(Peca peca, PecaRequest request) {
        peca.setNome(request.nome());
        peca.setFabricante(request.fabricante());
        peca.setValorBase(request.valorBase());
    }

    public static PecaResponse toResponse(Peca peca) {
        return paraPecaResponse(peca);
    }

    public static Peca toEntity(PecaRequest request) {
        return paraPeca(request);
    }

    public static void updateEntity(Peca peca, PecaRequest request) {
        atualizarEntidade(peca, request);
    }
}
