package com.cartoon.api.ordemServico.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Objects;

@Component
public class CalculadoraOrcamento {

    private static final BigDecimal DIVISOR_FIPE = new BigDecimal("100000");

    /**
     * Converte LocalTime (tempo recebido como DateTime/Time) para horas decimais.
     * Exemplo: 01:30 -> 1.5 horas, 00:45 -> 0.75 horas
     */
    public double converterParaDecimal(LocalTime tempo) {
        Objects.requireNonNull(tempo, "tempo");
        return tempo.getHour() + (tempo.getMinute() / 60.0) + (tempo.getSecond() / 3600.0);
    }

    /**
     * Preço de uma unidade do serviço a partir de LocalTime (recebido como DateTime):
     * Converte hora para decimal e aplica a fórmula:
     * Horas decimais x valorHora x (1 + (FIPE / 100000) x taxaRisco)
     */
    public BigDecimal valorServico(LocalTime tempo, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco) {
        Objects.requireNonNull(tempo, "tempo");
        double horasDecimais = converterParaDecimal(tempo);
        return valorServico(horasDecimais, valorHora, valorVeiculo, taxaRisco);
    }

    public BigDecimal valorServico(LocalTime tempo, BigDecimal valorHora, BigDecimal valorVeiculo) {
        return valorServico(tempo, valorHora, valorVeiculo, BigDecimal.ZERO);
    }

    /**
     * Preço de uma unidade do serviço a partir de horas decimais:
     * Horas x valorHora x (1 + (FIPE / 100000) x taxaRisco)
     */
    public BigDecimal valorServico(Double horas, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco) {
        Objects.requireNonNull(horas, "horas");
        Objects.requireNonNull(valorHora, "valorHora");
        Objects.requireNonNull(valorVeiculo, "valorVeiculo");

        BigDecimal taxa = taxaRisco != null ? taxaRisco : BigDecimal.ZERO;
        BigDecimal fatorFipe = valorVeiculo.divide(DIVISOR_FIPE, 8, RoundingMode.HALF_UP);
        BigDecimal adicionalRisco = fatorFipe.multiply(taxa);
        BigDecimal multiplicador = BigDecimal.ONE.add(adicionalRisco);

        BigDecimal base = BigDecimal.valueOf(horas).multiply(valorHora);
        return base.multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorServico(Double horas, BigDecimal valorHora, BigDecimal valorVeiculo) {
        return valorServico(horas, valorHora, valorVeiculo, BigDecimal.ZERO);
    }
}
