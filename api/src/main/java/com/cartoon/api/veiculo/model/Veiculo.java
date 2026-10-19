package com.cartoon.api.veiculo.model;

import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.ordemServico.models.OrdemServico;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "veiculo")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, unique = true, length = 10)
    String placa;

    @Column(nullable = false)
    String marca;

    @Column(nullable = false)
    String modelo;

    @Column(name = "ano_modelo", nullable = false)
    Integer anoModelo;

    @Column(name = "combustivel")
    String combustivel;

    @Column(name = "codigo_fipe", length = 20)
    String codigoFipe;

    @Column(name = "valor_fipe", precision = 12, scale = 2)
    BigDecimal valorFipe;

    @Column(name = "mes_referencia", length = 50)
    String mesReferencia;

    @Column(nullable = false)
    Boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    Cliente cliente;

    @OneToMany(mappedBy = "veiculo", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderBy("dataOrcamento DESC, id DESC")
    List<OrdemServico> ordensServicos = new ArrayList<>();

    public void setPlaca(String placa) {
        if (placa != null) {
            this.placa = placa.trim().toUpperCase();
        } else {
            this.placa = null;
        }
    }

    public String getMontadora() {
        return this.marca;
    }

    public void setMontadora(String montadora) {
        this.marca = montadora;
    }

    public Integer getAno() {
        return this.anoModelo;
    }

    public void setAno(Integer ano) {
        this.anoModelo = ano;
    }
}
