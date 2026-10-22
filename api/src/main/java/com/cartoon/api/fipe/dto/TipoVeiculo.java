package com.cartoon.api.fipe.dto;

import com.cartoon.api.compartilhado.exceptions.TipoVeiculoInvalidoException;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.text.Normalizer;
import java.util.Locale;

public enum TipoVeiculo {
    CARROS("cars"),
    MOTOS("motorcycles"),
    CAMINHOES("trucks");

    private final String fipePath;

    TipoVeiculo(String fipePath) {
        this.fipePath = fipePath;
    }

    public String getFipePath() {
        return fipePath;
    }

    @JsonCreator
    public static TipoVeiculo from(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new TipoVeiculoInvalidoException(String.valueOf(valor));
        }

        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        return switch (normalizado) {
            case "carro", "carros", "car", "cars" -> CARROS;
            case "moto", "motos", "motorcycle", "motorcycles" -> MOTOS;
            case "caminhao", "caminhoes", "truck", "trucks" -> CAMINHOES;
            default -> throw new TipoVeiculoInvalidoException(valor);
        };
    }
}
