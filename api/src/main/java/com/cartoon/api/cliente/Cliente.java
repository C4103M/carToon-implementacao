package com.cartoon.api.cliente;

import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.PackagePrivate;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false, unique = true, length = 11)
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
