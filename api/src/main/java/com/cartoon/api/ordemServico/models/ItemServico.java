package com.cartoon.api.ordemServico.models;

import com.cartoon.api.servico.Servico;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class ItemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column(nullable = false)
    Integer quantidade;
    @Column(name = "valor_unitario", nullable = false)
    Double valorUnitario;

    Double subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    Servico servico;

    LocalTime tempo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;
}
