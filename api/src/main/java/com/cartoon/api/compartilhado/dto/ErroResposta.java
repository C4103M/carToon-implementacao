package com.cartoon.api.compartilhado.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        Map<String, String> errosValidacao
) {
    public ErroResposta(Integer status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null);
    }

    public ErroResposta(Integer status, String error, String message, Map<String, String> errosValidacao) {
        this(LocalDateTime.now(), status, error, message, errosValidacao);
    }
}
