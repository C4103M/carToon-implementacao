package com.cartoon.api.oficina.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "oficina", uniqueConstraints = {
        @UniqueConstraint(name = "uk_oficina_nome", columnNames = "nome"),
        @UniqueConstraint(name = "uk_oficina_telefone", columnNames = "telefone")
})
public class Oficina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String nome;

    String endereco;
    String telefone;

    @Column(nullable = false)
    Boolean ativo = true;


}
