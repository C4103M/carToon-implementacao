package com.cartoon.api.ordemServico.models;

import com.cartoon.api.servico.models.Servico;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
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
    BigDecimal valorUnitario;

    BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    Servico servico;

    @Column(name = "tempo")
    LocalTime tempo;

    @Column(name = "peso_servico", precision = 5, scale = 2)
    BigDecimal pesoServico = new BigDecimal("0.50");

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    OrdemServico ordemServico;

    public void setPesoServico(BigDecimal pesoServico) {
        this.pesoServico = pesoServico != null ? pesoServico : new BigDecimal("0.5");
    }

    /**
     * Retorna a quantidade de horas decimais calculada dinamicamente a partir do tempo (LocalTime).
     */
    public Double getQuantidadeHoras() {
        return Servico.converterParaHorasDecimais(this.tempo);
    }

    /**
     * Define o tempo (LocalTime) a partir de horas decimais.
     */
    public void setQuantidadeHoras(Double quantidadeHoras) {
        this.tempo = Servico.converterParaLocalTime(quantidadeHoras);
    }

    public void calcSubtotal() {
        if (valorUnitario != null && quantidade != null) {
            this.subtotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }
}
