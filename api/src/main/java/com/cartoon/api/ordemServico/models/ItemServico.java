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

    @Column(name = "quantidade_horas")
    Double quantidadeHoras;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

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

    public void calcSubtotal() {
        if (valorUnitario != null && quantidade != null) {
            this.subtotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
        }
    }
}
