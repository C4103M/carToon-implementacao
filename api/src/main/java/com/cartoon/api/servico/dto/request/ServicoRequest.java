package com.cartoon.api.servico.dto.request;

import com.cartoon.api.servico.dto.deserializer.TempoDeserializer;
import com.cartoon.api.servico.models.Servico;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

public record ServicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O tempo é obrigatório")
        @JsonDeserialize(using = TempoDeserializer.class)
        @JsonAlias({"quantidadeHoras", "hora", "horas", "tempoEstimado"})
        LocalTime tempo,

        String descricao,

        @DecimalMin(value = "0.0", inclusive = true, message = "O peso do serviço deve ser no mínimo 0.0")
        @DecimalMax(value = "1.0", inclusive = true, message = "O peso do serviço deve ser no máximo 1.0")
        @JsonAlias({"peso", "peso_servico"})
        BigDecimal pesoServico
) {
    private static final BigDecimal PESO_PADRAO = new BigDecimal("0.5");

    @JsonCreator
    public ServicoRequest(
            @JsonProperty("nome") String nome,
            @JsonProperty("tempo") @JsonDeserialize(using = TempoDeserializer.class) LocalTime tempo,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("pesoServico") BigDecimal pesoServico
    ) {
        this.nome = nome;
        this.tempo = tempo;
        this.descricao = descricao;
        this.pesoServico = pesoServico != null ? pesoServico : PESO_PADRAO;
    }

    public ServicoRequest(
            String nome,
            LocalTime tempo,
            String descricao
    ) {
        this(nome, tempo, descricao, PESO_PADRAO);
    }

    public ServicoRequest(String nome, Double quantidadeHoras, String descricao) {
        this(nome, converterDoubleParaLocalTime(quantidadeHoras), descricao, PESO_PADRAO);
    }

    public ServicoRequest(String nome, Double quantidadeHoras, String descricao, BigDecimal pesoServico) {
        this(nome, converterDoubleParaLocalTime(quantidadeHoras), descricao, pesoServico != null ? pesoServico : PESO_PADRAO);
    }

    public Double quantidadeHoras() {
        return Servico.converterParaHorasDecimais(tempo);
    }

    private static LocalTime converterDoubleParaLocalTime(Double horas) {
        return Servico.converterParaLocalTime(horas);
    }
}
