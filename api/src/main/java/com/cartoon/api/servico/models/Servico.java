package com.cartoon.api.servico.models;

import com.cartoon.api.oficina.model.Oficina;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class Servico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(name = "tempo")
    LocalTime tempo;

    String descricao;

    @Column(name = "peso_servico", precision = 3, scale = 1)
    BigDecimal pesoServicoPadrao = new BigDecimal("0.5");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    Oficina oficina;

    /**
     * Construtor principal canônico.
     */
    public Servico(Integer id, String nome, LocalTime tempo, String descricao, BigDecimal pesoServico) {
        this.id = id;
        this.nome = nome;
        this.tempo = tempo;
        this.descricao = descricao;
        setPesoServicoPadrao(pesoServico);
    }

    /**
     * Sobrecarga delegando com peso padrão (0.5).
     */
    public Servico(Integer id, String nome, LocalTime tempo, String descricao) {
        this(id, nome, tempo, descricao, new BigDecimal("0.5"));
    }

    /**
     * Sobrecarga de conveniência aceitando horas decimais (converte automaticamente no construtor).
     */
    public Servico(Integer id, String nome, Double quantidadeHoras, String descricao) {
        this(id, nome, converterParaLocalTime(quantidadeHoras), descricao, new BigDecimal("0.5"));
    }

    public void setPesoServicoPadrao(BigDecimal pesoServicoPadrao) {
        this.pesoServicoPadrao = pesoServicoPadrao != null ? pesoServicoPadrao : new BigDecimal("0.5");
    }

    public BigDecimal getPesoServico() {
        return getPesoServicoPadrao();
    }

    public void setPesoServico(BigDecimal pesoServico) {
        setPesoServicoPadrao(pesoServico);
    }

    /**
     * Retorna a quantidade de horas decimais calculada dinamicamente a partir do tempo (LocalTime).
     */
    public Double getQuantidadeHoras() {
        return converterParaHorasDecimais(this.tempo);
    }

    /**
     * Define o tempo (LocalTime) a partir de horas decimais.
     */
    public void setQuantidadeHoras(Double quantidadeHoras) {
        this.tempo = converterParaLocalTime(quantidadeHoras);
    }

    /**
     * Método da classe Model para converter LocalTime para horas decimais.
     * Exemplo: 01:30:00 -> 1.5
     */
    public static Double converterParaHorasDecimais(LocalTime tempo) {
        if (tempo == null) {
            return null;
        }
        return tempo.getHour() + (tempo.getMinute() / 60.0) + (tempo.getSecond() / 3600.0);
    }

    /**
     * Método da classe Model para converter horas decimais para LocalTime.
     * Exemplo: 1.5 -> 01:30:00
     */
    public static LocalTime converterParaLocalTime(Double horas) {
        if (horas == null) {
            return null;
        }
        int h = horas.intValue();
        int m = (int) Math.round((horas - h) * 60);
        if (m >= 60) {
            h += m / 60;
            m = m % 60;
        }
        return LocalTime.of(Math.min(h, 23), Math.min(m, 59));
    }
}
