package com.cartoon.api.peca.models;

import com.cartoon.api.oficina.model.Oficina;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class Peca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false)
    String fabricante;

    @Column(name = "valor_base", nullable = false)
    BigDecimal valorBase;

    @Column(nullable = false)
    Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oficina_id", nullable = false)
    Oficina oficina;

    public Peca(Integer id, String nome, String fabricante, BigDecimal valorBase) {
        this.id = id;
        this.nome = nome;
        this.fabricante = fabricante;
        this.valorBase = valorBase;
        this.ativo = true;
    }
}
