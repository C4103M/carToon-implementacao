package com.cartoon.api.veiculo;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.ordemServico.models.OrdemServico;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false)
    String placa;
    @Column(nullable = false)
    String modelo;
    @Column(nullable = false)
    Integer ano;
    @Column(nullable = false)
    String montadora;
    @Column(name = "valor_fipe")
    Double valorFipe;
    @Column(nullable = false)
    Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    Cliente cliente;

    @OneToMany(mappedBy = "veiculo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    List<OrdemServico> ordensServicos = new ArrayList<>();



}
