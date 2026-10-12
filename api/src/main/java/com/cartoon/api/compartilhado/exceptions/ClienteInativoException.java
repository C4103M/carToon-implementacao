package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma operação envolve um cliente inativo.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ClienteInativoException extends RecursoInativoException {
    public ClienteInativoException() {
        super("Cliente inativo no sistema.");
    }

    public ClienteInativoException(String message) {
        super(message);
    }

    public ClienteInativoException(Integer clienteId) {
        super("Cliente", clienteId);
    }
}
