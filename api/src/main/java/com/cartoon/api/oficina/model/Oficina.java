package com.cartoon.api.oficina.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "oficina", uniqueConstraints = {
        @UniqueConstraint(name = "uk_oficina_nome", columnNames = "nome"),
        @UniqueConstraint(name = "uk_oficina_telefone", columnNames = "telefone"),
        @UniqueConstraint(name = "uk_oficina_email", columnNames = "email")
})
public class Oficina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String nome;

    String endereco;
    String telefone;
    String email;

    @Column(name = "valor_hora", precision = 12, scale = 2)
    BigDecimal valorHora = new BigDecimal("100.00");

    @Column(name = "taxa_risco", precision = 6, scale = 4)
    BigDecimal taxaRisco = new BigDecimal("0.10");

    @Column(nullable = false)
    Boolean ativo = true;


}
