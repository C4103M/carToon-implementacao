package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma operação envolve um recurso inativo no sistema.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecursoInativoException extends RegraDeNegocioException {
    public RecursoInativoException(String message) {
        super(message);
    }

    public RecursoInativoException(String recurso, Object id) {
        super(String.format("%s inativo(a) no sistema: %s", recurso, id));
    }
}
