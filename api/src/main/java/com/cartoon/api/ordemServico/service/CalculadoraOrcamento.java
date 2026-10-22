package com.cartoon.api.ordemServico.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Component
public class CalculadoraOrcamento {

    static final BigDecimal PERCENTUAL_ADICIONAL_VEICULO = new BigDecimal("0.01");

    /** Preço de uma unidade do serviço: valor base + 1% do valor do veículo. */
    public BigDecimal valorServico(BigDecimal valorBase, BigDecimal valorVeiculo) {
        Objects.requireNonNull(valorBase, "valorBase");
        Objects.requireNonNull(valorVeiculo, "valorVeiculo");
        BigDecimal adicional = valorVeiculo.multiply(PERCENTUAL_ADICIONAL_VEICULO);
        return valorBase.add(adicional).setScale(2, RoundingMode.HALF_UP);
    }
}