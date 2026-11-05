package com.cartoon.api.cliente.model;

import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.veiculo.model.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cliente", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cliente_cpf_oficina", columnNames = {"cpf", "oficina_id"})
})
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false, length = 11)
    String cpf;

    String endereco;

    String telefone;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oficina_id", nullable = false)
    Oficina oficina;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    List<Veiculo> veiculos;
}