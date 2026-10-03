package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma placa de veículo possui formato inválido.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PlacaInvalidaException extends RegraDeNegocioException {
    public PlacaInvalidaException() {
        super("Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul.");
    }

    public PlacaInvalidaException(String message) {
        super(message);
    }
}
