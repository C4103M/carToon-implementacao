package com.cartoon.api.ordemServico.service;

import com.cartoon.api.servico.models.Servico;
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
        return Servico.converterParaHorasDecimais(tempo);
    }

    /**
     * Preço de uma unidade do serviço a partir de LocalTime (recebido como DateTime) com peso do serviço:
     * Converte hora para decimal e aplica a fórmula:
     * Horas decimais x valorHora x (1 + (FIPE / 100000) x taxaRisco x pesoServico)
     */
    public BigDecimal valorServico(LocalTime tempo, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco, BigDecimal pesoServico) {
        Objects.requireNonNull(tempo, "tempo");
        double horasDecimais = converterParaDecimal(tempo);
        return valorServico(horasDecimais, valorHora, valorVeiculo, taxaRisco, pesoServico);
    }

    public BigDecimal valorServico(LocalTime tempo, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco) {
        return valorServico(tempo, valorHora, valorVeiculo, taxaRisco, BigDecimal.ONE);
    }

    public BigDecimal valorServico(LocalTime tempo, BigDecimal valorHora, BigDecimal valorVeiculo) {
        return valorServico(tempo, valorHora, valorVeiculo, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Preço de uma unidade do serviço a partir de horas decimais e peso do serviço:
     * Horas x valorHora x (1 + (FIPE / 100000) x taxaRisco x pesoServico)
     *
     * Níveis de peso sugeridos:
     * 0.0: Sem risco extra (óleo, alinhamento, bateria, lâmpada)
     * 0.5: Risco moderado (freios, suspensão)
     * 1.0: Risco alto (embreagem, correia dentada, motor, câmbio, parte elétrica)
     */
    public BigDecimal valorServico(Double horas, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco, BigDecimal pesoServico) {
        Objects.requireNonNull(horas, "horas");
        Objects.requireNonNull(valorHora, "valorHora");
        Objects.requireNonNull(valorVeiculo, "valorVeiculo");

        BigDecimal taxa = taxaRisco != null ? taxaRisco : BigDecimal.ZERO;
        BigDecimal peso = pesoServico != null ? pesoServico : new BigDecimal("0.5");
        BigDecimal fatorFipe = valorVeiculo.divide(DIVISOR_FIPE, 8, RoundingMode.HALF_UP);
        BigDecimal adicionalRisco = fatorFipe.multiply(taxa).multiply(peso);
        BigDecimal multiplicador = BigDecimal.ONE.add(adicionalRisco);

        BigDecimal base = BigDecimal.valueOf(horas).multiply(valorHora);
        return base.multiply(multiplicador).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorServico(Double horas, BigDecimal valorHora, BigDecimal valorVeiculo, BigDecimal taxaRisco) {
        return valorServico(horas, valorHora, valorVeiculo, taxaRisco, BigDecimal.ONE);
    }

    public BigDecimal valorServico(Double horas, BigDecimal valorHora, BigDecimal valorVeiculo) {
        return valorServico(horas, valorHora, valorVeiculo, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Preço de uma unidade da peça a partir do valor base e da margem da oficina:
     * valorBase x margemPeca
     * Por padrão margemPeca é 1.0 (ex: 10% de margem -> 1.10)
     */
    public BigDecimal valorPeca(BigDecimal valorBase, BigDecimal margemPeca) {
        Objects.requireNonNull(valorBase, "valorBase");
        BigDecimal margem = (margemPeca != null && margemPeca.compareTo(BigDecimal.ONE) >= 0)
                ? margemPeca
                : BigDecimal.ONE;
        return valorBase.multiply(margem).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal valorPeca(BigDecimal valorBase) {
        return valorPeca(valorBase, BigDecimal.ONE);
    }
}
