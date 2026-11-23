package com.cartoon.api.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import com.cartoon.api.usuario.dto.request.AdminRequest;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.dto.request.MecanicoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

class UsuarioValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==========================================
    // Testes de Bean Validation: AdminRequest
    // ==========================================

    @Test
    @DisplayName("AdminRequest válido quando senhas coincidem")
    void adminRequest_valido() {
        AdminRequest request = new AdminRequest(
                "Gestor Admin",
                "admin@oficina.com",
                "senhaForte123",
                "senhaForte123",
                1
        );

        Set<ConstraintViolation<AdminRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AdminRequest inválido quando senhas divergem")
    void adminRequest_senhasDivergentes_deveFalhar() {
        AdminRequest request = new AdminRequest(
                "Gestor Admin",
                "admin@oficina.com",
                "senhaForte123",
                "outraSenhaDiferente",
                1
        );

        Set<ConstraintViolation<AdminRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("confirmarSenha")
                        && v.getMessage().equals("As senhas não coincidem."));
    }

    @Test
    @DisplayName("AdminRequest inválido quando senha tem menos de 8 caracteres")
    void adminRequest_senhaCurta_deveFalhar() {
        AdminRequest request = new AdminRequest(
                "Gestor Admin",
                "admin@oficina.com",
                "123",
                "123",
                1
        );

        Set<ConstraintViolation<AdminRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("senha"));
    }

    // ==========================================
    // Testes de Bean Validation: MecanicoRequest
    // ==========================================

    @Test
    @DisplayName("MecanicoRequest válido quando senhas coincidem")
    void mecanicoRequest_valido() {
        MecanicoRequest request = new MecanicoRequest(
                "Carlos Mecânico",
                "carlos@oficina.com",
                "senhaForte123",
                "senhaForte123"
        );

        Set<ConstraintViolation<MecanicoRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("MecanicoRequest inválido quando confirmação é divergente")
    void mecanicoRequest_senhasDivergentes_deveFalhar() {
        MecanicoRequest request = new MecanicoRequest(
                "Carlos Mecânico",
                "carlos@oficina.com",
                "senhaForte123",
                "senhaErrada"
        );

        Set<ConstraintViolation<MecanicoRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("confirmarSenha")
                        && v.getMessage().equals("As senhas não coincidem."));
    }

    // ==================================================
    // Testes de Bean Validation: AtualizarUsuarioRequest
    // ==================================================

    @Test
    @DisplayName("AtualizarUsuarioRequest válido sem informar senha")
    void atualizarUsuario_semSenha_valido() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Novo Nome",
                "novo@oficina.com",
                null,
                null
        );

        Set<ConstraintViolation<AtualizarUsuarioRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AtualizarUsuarioRequest válido com nova senha e confirmação idênticas")
    void atualizarUsuario_comSenhaCoincidente_valido() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Novo Nome",
                "novo@oficina.com",
                "novaSenha123",
                "novaSenha123"
        );

        Set<ConstraintViolation<AtualizarUsuarioRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AtualizarUsuarioRequest inválido quando nova senha e confirmação divergem")
    void atualizarUsuario_comSenhaDivergente_deveFalhar() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest(
                "Novo Nome",
                "novo@oficina.com",
                "novaSenha123",
                "outraSenha456"
        );

        Set<ConstraintViolation<AtualizarUsuarioRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("confirmarSenha")
                        && v.getMessage().equals("As senhas não coincidem."));
    }
}
