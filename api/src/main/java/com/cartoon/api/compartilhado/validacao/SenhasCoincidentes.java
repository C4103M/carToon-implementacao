package com.cartoon.api.compartilhado.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SenhasCoincidentesValidator.class)
@Documented
public @interface SenhasCoincidentes {
    String message() default "As senhas não coincidem.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
