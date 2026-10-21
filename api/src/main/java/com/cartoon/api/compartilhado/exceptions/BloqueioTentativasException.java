package com.cartoon.api.compartilhado.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando o número de tentativas consecutivas de autenticação é excedido.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BloqueioTentativasException extends RegraDeNegocioException {

    public static final String MENSAGEM_PADRAO = "Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos.";

    public BloqueioTentativasException() {
        super(MENSAGEM_PADRAO);
    }

    public BloqueioTentativasException(String message) {
        super(message);
    }
}
