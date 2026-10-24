package com.cartoon.api.servico.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ServicoResponse(
        Integer id,
        String nome,
        @JsonFormat(pattern = "HH:mm")
        LocalTime tempo,
        Double quantidadeHoras,
        String descricao,
        Integer oficinaId
) {
    public ServicoResponse(Integer id, String nome, LocalTime tempo, String descricao) {
        this(id, nome, tempo, tempo != null ? tempo.getHour() + (tempo.getMinute() / 60.0) + (tempo.getSecond() / 3600.0) : null, descricao, null);
    }

    public ServicoResponse(Integer id, String nome, LocalTime tempo, Double quantidadeHoras, String descricao) {
        this(id, nome, tempo, quantidadeHoras, descricao, null);
    }

    public ServicoResponse(Integer id, String nome, Double quantidadeHoras, String descricao) {
        this(id, nome, converterDoubleParaLocalTime(quantidadeHoras), quantidadeHoras, descricao, null);
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
