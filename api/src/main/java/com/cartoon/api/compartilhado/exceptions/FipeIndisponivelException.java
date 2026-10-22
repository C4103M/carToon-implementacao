package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando o serviço externo da FIPE está indisponível ou falha.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class FipeIndisponivelException extends RuntimeException {

    public FipeIndisponivelException() {
        super("Serviço da tabela FIPE temporariamente indisponível. Tente novamente mais tarde.");
    }

    public FipeIndisponivelException(String message) {
        super(message);
    }

    public FipeIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
