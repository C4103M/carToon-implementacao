package com.cartoon.api.compartilhado.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SenhasCoincidentesValidator implements ConstraintValidator<SenhasCoincidentes, PossuiConfirmacaoSenha> {

    private String message;

    @Override
    public void initialize(SenhasCoincidentes constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(PossuiConfirmacaoSenha obj, ConstraintValidatorContext context) {
        if (obj == null) {
            return true;
        }

        String senha = obj.senha();
        String confirmarSenha = obj.confirmarSenha();

        // Se ambos forem nulos ou vazios (ex: atualização de dados sem trocar senha):
        if ((senha == null || senha.isBlank()) && (confirmarSenha == null || confirmarSenha.isBlank())) {
            return true;
        }

        boolean coincidem = Objects.equals(senha, confirmarSenha);

        if (!coincidem) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode("confirmarSenha")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
