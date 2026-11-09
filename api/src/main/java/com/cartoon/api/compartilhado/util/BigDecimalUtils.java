package com.cartoon.api.compartilhado.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class BigDecimalUtils {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final int CASAS_MONETARIAS = 2;
    private static final int CASAS_PERCENTUAIS = 1;

    private BigDecimalUtils() {
    }

    public static BigDecimal nuloComoZero(BigDecimal valor) {
        return Objects.requireNonNullElse(valor, BigDecimal.ZERO);
    }

    public static BigDecimal dinheiro(BigDecimal valor) {
        return nuloComoZero(valor).setScale(CASAS_MONETARIAS, RoundingMode.HALF_UP);
    }

    public static BigDecimal zeroMonetario() {
        return BigDecimal.ZERO.setScale(CASAS_MONETARIAS);
    }

    public static BigDecimal dividir(BigDecimal valor, long quantidade) {
        if (quantidade == 0) {
            return zeroMonetario();
        }
        return nuloComoZero(valor).divide(BigDecimal.valueOf(quantidade), CASAS_MONETARIAS, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_PERCENTUAIS);
        }
        return nuloComoZero(parte).multiply(CEM).divide(total, CASAS_PERCENTUAIS, RoundingMode.HALF_UP);
    }

    public static BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return null;
        }
        return nuloComoZero(atual).subtract(anterior).multiply(CEM).divide(anterior, CASAS_PERCENTUAIS, RoundingMode.HALF_UP);
    }
}
