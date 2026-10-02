package com.cartoon.api.compartilhado.exceptions;

/**
 * Exceção autoexplicativa lançada quando uma regra de negócio da aplicação é violada.
 * Substitui o antigo ValidacaoException para evitar ambiguidade com o Bean Validation do Spring/Jakarta.
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
