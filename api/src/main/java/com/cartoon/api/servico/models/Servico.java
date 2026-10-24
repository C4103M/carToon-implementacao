package com.cartoon.api.servico.models;

import com.cartoon.api.oficina.model.Oficina;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "quantidade_horas")
    Double quantidadeHoras;

    String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    Oficina oficina;

    public Servico(Integer id, String nome, Double quantidadeHoras, String descricao) {
        this.id = id;
        this.nome = nome;
        setQuantidadeHoras(quantidadeHoras);
        this.descricao = descricao;
    }

    public Servico(Integer id, String nome, LocalTime tempo, String descricao) {
        this.id = id;
        this.nome = nome;
        setTempo(tempo);
        this.descricao = descricao;
    }

    public void setTempo(LocalTime tempo) {
        this.tempo = tempo;
        if (tempo != null) {
            this.quantidadeHoras = tempo.getHour() + (tempo.getMinute() / 60.0) + (tempo.getSecond() / 3600.0);
        } else {
            this.quantidadeHoras = null;
        }
    }

    public void setQuantidadeHoras(Double quantidadeHoras) {
        this.quantidadeHoras = quantidadeHoras;
        if (quantidadeHoras != null) {
            int h = quantidadeHoras.intValue();
            int m = (int) Math.round((quantidadeHoras - h) * 60);
            if (m >= 60) {
                h += m / 60;
                m = m % 60;
            }
            this.tempo = LocalTime.of(Math.min(h, 23), Math.min(m, 59));
        } else {
            this.tempo = null;
        }
    }
}
