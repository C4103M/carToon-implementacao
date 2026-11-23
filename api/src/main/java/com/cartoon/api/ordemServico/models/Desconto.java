package com.cartoon.api.ordemServico.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum Desconto {
    SEM_DESCONTO(BigDecimal.ZERO, 0, "0%"),
    CINCO_PORCENTO(new BigDecimal("0.05"), 5, "5%"),
    DEZ_PORCENTO(new BigDecimal("0.10"), 10, "10%"),
    QUINZE_PORCENTO(new BigDecimal("0.15"), 15, "15%");

    private final BigDecimal percentual;
    private final int porcentagem;
    private final String descricao;

    Desconto(BigDecimal percentual, int porcentagem, String descricao) {
        this.percentual = percentual;
        this.porcentagem = porcentagem;
        this.descricao = descricao;
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    public int getPorcentagem() {
        return porcentagem;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal calcularValorDesconto(BigDecimal valorBruto) {
        if (valorBruto == null || valorBruto.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valorBruto.multiply(percentual).setScale(2, RoundingMode.HALF_UP);
    }

    @JsonCreator
    public static Desconto fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return SEM_DESCONTO;
        }
        String normalizado = valor.trim().toUpperCase().replace("%", "");
        return switch (normalizado) {
            case "0", "ZERO", "SEM_DESCONTO" -> SEM_DESCONTO;
            case "5", "CINCO", "CINCO_PORCENTO" -> CINCO_PORCENTO;
            case "10", "DEZ", "DEZ_PORCENTO" -> DEZ_PORCENTO;
            case "15", "QUINZE", "QUINZE_PORCENTO" -> QUINZE_PORCENTO;
            default -> {
                try {
                    int num = Integer.parseInt(normalizado);
                    if (num > 15) {
                        throw new IllegalArgumentException("O desconto não pode ultrapassar 15%.");
                    }
                } catch (NumberFormatException ignored) {}
                throw new IllegalArgumentException("Desconto inválido: " + valor + ". Opções válidas: SEM_DESCONTO (0%), CINCO_PORCENTO (5%), DEZ_PORCENTO (10%), QUINZE_PORCENTO (15%).");
            }
        };
    }
}
