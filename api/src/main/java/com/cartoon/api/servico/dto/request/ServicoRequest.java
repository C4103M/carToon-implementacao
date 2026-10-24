package com.cartoon.api.servico.dto.request;

import com.cartoon.api.servico.dto.deserializer.TempoDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ServicoRequest(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotNull(message = "O tempo é obrigatório")
        @JsonDeserialize(using = TempoDeserializer.class)
        @JsonAlias({"quantidadeHoras", "hora", "horas", "tempoEstimado"})
        LocalTime tempo,

        String descricao
) {
    @JsonCreator
    public ServicoRequest(
            @JsonProperty("nome") String nome,
            @JsonProperty("tempo") @JsonDeserialize(using = TempoDeserializer.class) LocalTime tempo,
            @JsonProperty("descricao") String descricao
    ) {
        this.nome = nome;
        this.tempo = tempo;
        this.descricao = descricao;
    }

    public ServicoRequest(String nome, Double quantidadeHoras, String descricao) {
        this(nome, converterDoubleParaLocalTime(quantidadeHoras), descricao);
    }

    public Double quantidadeHoras() {
        if (tempo == null) return null;
        return tempo.getHour() + (tempo.getMinute() / 60.0) + (tempo.getSecond() / 3600.0);
    }

    private static LocalTime converterDoubleParaLocalTime(Double horas) {
        if (horas == null) return null;
        int h = horas.intValue();
        int m = (int) Math.round((horas - h) * 60);
        if (m >= 60) {
            h += m / 60;
            m = m % 60;
        }
        return LocalTime.of(Math.min(h, 23), Math.min(m, 59));
    }
}
