package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando o tipo de veículo informado para consulta FIPE é inválido.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TipoVeiculoInvalidoException extends RegraDeNegocioException {

    public TipoVeiculoInvalidoException(String tipo) {
        super("Tipo de veículo inválido: " + tipo + ". Valores aceitos: carros, motos, caminhoes.");
    }
}
