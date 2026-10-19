package com.cartoon.api.fipe.client;

import com.cartoon.api.fipe.dto.FipeItemResponse;
import com.cartoon.api.fipe.dto.FipeVeiculoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FipeClient {

    private final RestClient fipeRestClient;

    public List<FipeItemResponse> buscarMarcas(String tipoPath) {
        return fipeRestClient.get()
                .uri("/{tipo}/brands", tipoPath)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<FipeItemResponse> buscarModelos(String tipoPath, String marcaId) {
        return fipeRestClient.get()
                .uri("/{tipo}/brands/{marcaId}/models", tipoPath, marcaId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<FipeItemResponse> buscarAnos(String tipoPath, String marcaId, String modeloId) {
        return fipeRestClient.get()
                .uri("/{tipo}/brands/{marcaId}/models/{modeloId}/years", tipoPath, marcaId, modeloId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public FipeVeiculoResponse buscarValor(String tipoPath, String marcaId, String modeloId, String anoId) {
        return fipeRestClient.get()
                .uri("/{tipo}/brands/{marcaId}/models/{modeloId}/years/{anoId}", tipoPath, marcaId, modeloId, anoId)
                .retrieve()
                .body(FipeVeiculoResponse.class);
    }
}
