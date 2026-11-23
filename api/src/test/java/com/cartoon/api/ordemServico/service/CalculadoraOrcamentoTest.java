package com.cartoon.api.ordemServico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraOrcamentoTest {

    private final CalculadoraOrcamento calc = new CalculadoraOrcamento();

    @Test
    @DisplayName("Deve calcular valor unitário do serviço usando horas x valorHora x (1 + FIPE/100000 x taxaRisco x pesoServico)")
    void deveCalcularValorUnitarioComHorasValorHoraFipeETaxaRisco() {
        // base = 2.0 * 100.00 = 200.00
        // multiplicador = 1 + (50000 / 100000) * 0.10 = 1 + 0.05 = 1.05
        // total = 200.00 * 1.05 = 210.00
        BigDecimal resultado = calc.valorServico(2.0, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("210.00"), resultado);
    }

    @Test
    @DisplayName("Deve calcular corretamente os 3 níveis de peso sugeridos: 0.0 (sem risco), 0.5 (moderado) e 1.0 (alto)")
    void deveCalcularOsTresNiveisDePesoSugeridos() {
        Double horas = 2.0;
        BigDecimal valorHora = new BigDecimal("100.00"); // Base = 200.00
        BigDecimal fipe = new BigDecimal("100000.00"); // FIPE / 100000 = 1.0
        BigDecimal taxaRisco = new BigDecimal("0.10"); // 10%

        // Nível 0.0: Sem risco extra -> 200 * (1 + 1.0 * 0.10 * 0.0) = 200.00
        BigDecimal resSemRisco = calc.valorServico(horas, valorHora, fipe, taxaRisco, BigDecimal.ZERO);
        assertEquals(new BigDecimal("200.00"), resSemRisco);

        // Nível 0.5: Risco moderado -> 200 * (1 + 1.0 * 0.10 * 0.5) = 200 * 1.05 = 210.00
        BigDecimal resModerado = calc.valorServico(horas, valorHora, fipe, taxaRisco, new BigDecimal("0.5"));
        assertEquals(new BigDecimal("210.00"), resModerado);

        // Nível 1.0: Risco alto -> 200 * (1 + 1.0 * 0.10 * 1.0) = 200 * 1.10 = 220.00
        BigDecimal resAlto = calc.valorServico(horas, valorHora, fipe, taxaRisco, BigDecimal.ONE);
        assertEquals(new BigDecimal("220.00"), resAlto);

        // Quando peso for nulo, fallback é 0.5 (moderado) -> 210.00
        BigDecimal resNulo = calc.valorServico(horas, valorHora, fipe, taxaRisco, null);
        assertEquals(new BigDecimal("210.00"), resNulo);
    }

    @Test
    @DisplayName("Quando taxaRisco for zero ou nula, deve retornar apenas horas x valorHora")
    void quandoTaxaRiscoZeroOuNulaDeveRetornarHorasVezesValorHora() {
        BigDecimal resultadoZero = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("150.00"), resultadoZero);

        BigDecimal resultadoNulo = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"), null);
        assertEquals(new BigDecimal("150.00"), resultadoNulo);

        BigDecimal resultadoSemTaxa = calc.valorServico(1.5, new BigDecimal("100.00"), new BigDecimal("50000.00"));
        assertEquals(new BigDecimal("150.00"), resultadoSemTaxa);
    }

    @Test
    @DisplayName("Veículo com FIPE mais cara deve gerar serviço proporcionalmente mais caro pela taxa de risco")
    void veiculoMaisCaroDeveGerarServicoMaisCaro() {
        BigDecimal v1 = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("100000.00"), new BigDecimal("0.10"));
        BigDecimal v2 = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("200000.00"), new BigDecimal("0.10"));

        assertEquals(new BigDecimal("110.00"), v1);
        assertEquals(new BigDecimal("120.00"), v2);
    }

    @Test
    @DisplayName("Deve arredondar o resultado para duas casas decimais com HALF_UP")
    void deveArredondarParaDuasCasas() {
        // base = 1.0 * 100.00 = 100.00
        // fipe = 33333.33 -> fator = 0.33333330
        // taxa = 0.1 -> adicional = 0.03333333 -> multiplicador = 1.03333333
        // 100 * 1.03333333 = 103.33
        BigDecimal resultado = calc.valorServico(1.0, new BigDecimal("100.00"), new BigDecimal("33333.33"), new BigDecimal("0.10"));
        assertEquals(new BigDecimal("103.33"), resultado);
    }

    @Test
    @DisplayName("Não deve aceitar parâmetros obrigatórios nulos")
    void naoDeveAceitarParametrosObrigatoriosNulos() {
        assertThrows(NullPointerException.class,
                () -> calc.valorServico((Double) null, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico(1.0, null, new BigDecimal("50000.00"), new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico(1.0, new BigDecimal("100.00"), null, new BigDecimal("0.10")));
        assertThrows(NullPointerException.class,
                () -> calc.valorServico((java.time.LocalTime) null, new BigDecimal("100.00"), new BigDecimal("50000.00"), new BigDecimal("0.10")));
    }

    @Test
    @DisplayName("Deve calcular valor do serviço a partir de LocalTime convertendo para decimal")
    void deveCalcularValorServicoLocalTime() {
        // 01:30 -> 1.5 horas: 1.5 * 100.00 * (1 + 0.05) = 157.50
        BigDecimal resultado = calc.valorServico(
                java.time.LocalTime.of(1, 30),
                new BigDecimal("100.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.10")
        );
        assertEquals(new BigDecimal("157.50"), resultado);

        // 00:45 -> 0.75 horas: 0.75 * 100 * 1.05 = 78.75
        BigDecimal resultado45 = calc.valorServico(
                java.time.LocalTime.of(0, 45),
                new BigDecimal("100.00"),
                new BigDecimal("50000.00"),
                new BigDecimal("0.10")
        );
        assertEquals(new BigDecimal("78.75"), resultado45);
    }

    @Test
    @DisplayName("Deve calcular valor unitário da peça aplicando a margem da oficina (ex: 10% em R$ 100 sai a R$ 110)")
    void deveCalcularValorPecaComMargem() {
        // Margem de 10% (1.10) sobre 100.00 -> 110.00
        BigDecimal resultado10Pct = calc.valorPeca(new BigDecimal("100.00"), new BigDecimal("1.10"));
        assertEquals(new BigDecimal("110.00"), resultado10Pct);

        // Padrão 1.0 (sem acréscimo) -> 100.00
        BigDecimal resultadoPadrao = calc.valorPeca(new BigDecimal("100.00"), new BigDecimal("1.00"));
        assertEquals(new BigDecimal("100.00"), resultadoPadrao);

        // Margem nula ou menor que 1.0 -> fallback para 1.0
        BigDecimal resultadoNulo = calc.valorPeca(new BigDecimal("100.00"), null);
        assertEquals(new BigDecimal("100.00"), resultadoNulo);

        BigDecimal resultadoMenorQue1 = calc.valorPeca(new BigDecimal("100.00"), new BigDecimal("0.80"));
        assertEquals(new BigDecimal("100.00"), resultadoMenorQue1);

        // Sobrecarga sem margem -> 1.0
        BigDecimal resultadoSemMargem = calc.valorPeca(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), resultadoSemMargem);
    }

    @Test
    @DisplayName("valorPeca - Não deve aceitar valorBase nulo")
    void valorPeca_NaoDeveAceitarValorBaseNulo() {
        assertThrows(NullPointerException.class, () -> calc.valorPeca(null, new BigDecimal("1.10")));
    }
}
