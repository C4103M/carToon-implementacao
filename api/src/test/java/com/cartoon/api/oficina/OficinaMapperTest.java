package com.cartoon.api.oficina;

import static org.assertj.core.api.Assertions.assertThat;

import com.cartoon.api.oficina.dto.request.OficinaRequest;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

class OficinaMapperTest {

    private final OficinaMapper mapper = new OficinaMapper();

    @Test
    void toEntity_deveNormalizarTelefone() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "(11) 91234-5678", "contato@oficinax.com");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getTelefone()).isEqualTo("11912345678");
    }

    @Test
    void toEntity_deveRemoverEspacosDoNome() {
        OficinaRequest request = new OficinaRequest("  Oficina X  ", "Rua A, 10", "11912345678", "contato@oficinax.com");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getNome()).isEqualTo("Oficina X");
    }

    @Test
    void toEntity_deveNormalizarEmailParaMinusculo() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "  CONTATO@OficinaX.com  ");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getEmail()).isEqualTo("contato@oficinax.com");
    }

    @Test
    void toEntity_novaOficinaDeveNascerAtivaEComMargemPecaPadrao1() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "contato@oficinax.com");

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getAtivo()).isTrue();
        assertThat(oficina.getMargemPeca()).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void toEntity_comValorHoraTaxaRiscoEMargemPeca() {
        OficinaRequest request = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "contato@oficinax.com",
                new BigDecimal("120.00"), new BigDecimal("0.15"), new BigDecimal("1.10"));

        Oficina oficina = mapper.toEntity(request);

        assertThat(oficina.getValorHora()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(oficina.getTaxaRisco()).isEqualByComparingTo(new BigDecimal("0.15"));
        assertThat(oficina.getMargemPeca()).isEqualByComparingTo(new BigDecimal("1.10"));
    }

    @Test
    void updateEntity_deveSubstituirOsDadosDaOficinaExistente() {
        Oficina oficina = new Oficina();
        oficina.setId(7);
        oficina.setNome("Nome Antigo");
        oficina.setEndereco("Rua Antiga, 1");
        oficina.setTelefone("11900000000");
        oficina.setEmail("antigo@oficina.com");
        oficina.setAtivo(true);
        OficinaRequest request = new OficinaRequest("Nome Novo", "Av. Nova, 99", "(11) 97777-6666", "novo@oficina.com",
                new BigDecimal("150.00"), new BigDecimal("0.08"), new BigDecimal("1.20"));

        mapper.updateEntity(oficina, request);

        assertThat(oficina.getId()).isEqualTo(7);
        assertThat(oficina.getNome()).isEqualTo("Nome Novo");
        assertThat(oficina.getEndereco()).isEqualTo("Av. Nova, 99");
        assertThat(oficina.getTelefone()).isEqualTo("11977776666");
        assertThat(oficina.getEmail()).isEqualTo("novo@oficina.com");
        assertThat(oficina.getValorHora()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(oficina.getTaxaRisco()).isEqualByComparingTo(new BigDecimal("0.08"));
        assertThat(oficina.getMargemPeca()).isEqualByComparingTo(new BigDecimal("1.20"));
        assertThat(oficina.getAtivo()).isTrue();
    }

    @Test
    void toResponse_deveCopiarTodosOsCampos() {
        Oficina oficina = new Oficina();
        oficina.setId(3);
        oficina.setNome("Oficina X");
        oficina.setEndereco("Rua A, 10");
        oficina.setTelefone("11912345678");
        oficina.setEmail("contato@oficinax.com");
        oficina.setValorHora(new BigDecimal("100.00"));
        oficina.setTaxaRisco(new BigDecimal("0.10"));
        oficina.setMargemPeca(new BigDecimal("1.15"));
        oficina.setAtivo(false);

        OficinaResponse response = mapper.toResponse(oficina);

        assertThat(response.id()).isEqualTo(3);
        assertThat(response.nome()).isEqualTo("Oficina X");
        assertThat(response.endereco()).isEqualTo("Rua A, 10");
        assertThat(response.telefone()).isEqualTo("11912345678");
        assertThat(response.email()).isEqualTo("contato@oficinax.com");
        assertThat(response.valorHora()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.taxaRisco()).isEqualByComparingTo(new BigDecimal("0.10"));
        assertThat(response.margemPeca()).isEqualByComparingTo(new BigDecimal("1.15"));
        assertThat(response.ativo()).isFalse();
    }

    @Test
    void validacao_margemPecaMaiorOuIgualA1_devePassar() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        OficinaRequest reqPadrao = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "contato@oficinax.com",
                new BigDecimal("100.00"), new BigDecimal("0.10"), new BigDecimal("1.00"));
        Set<ConstraintViolation<OficinaRequest>> violations1 = validator.validate(reqPadrao);
        assertThat(violations1).isEmpty();

        OficinaRequest reqAcima = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "contato@oficinax.com",
                new BigDecimal("100.00"), new BigDecimal("0.10"), new BigDecimal("1.25"));
        Set<ConstraintViolation<OficinaRequest>> violations2 = validator.validate(reqAcima);
        assertThat(violations2).isEmpty();
    }

    @Test
    void validacao_margemPecaMenorQue1_deveFalhar() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        OficinaRequest reqInvalido = new OficinaRequest("Oficina X", "Rua A, 10", "11912345678", "contato@oficinax.com",
                new BigDecimal("100.00"), new BigDecimal("0.10"), new BigDecimal("0.99"));
        Set<ConstraintViolation<OficinaRequest>> violations = validator.validate(reqInvalido);
        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("margemPeca")
                        && v.getMessage().contains("maior ou igual a 1"));
    }
}
