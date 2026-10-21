package com.cartoon.api.ordemServico.models;

import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.veiculo.model.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "descricao", nullable = false)
    String descricao;

    @CreationTimestamp
    @Column(name = "data_orcamento")
    LocalDate dataOrcamento;
    @Column(name = "data_rejeicao")
    LocalDate dataRejeicao;
    @Column(name = "data_inicio")
    LocalDate dataInicio;
    @Column(name = "data_finalizacao")
    LocalDate dataFinalizacao;

    BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    StatusServico statusServico = StatusServico.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oficina_id", nullable = false)
    Oficina oficina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mecanico_id", nullable = false)
    Usuario mecanico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    Veiculo veiculo;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ItemPeca> itensPeca = new ArrayList<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ItemServico> itensServico = new ArrayList<>();

    public void adicionarItemPeca(ItemPeca item) {
        itensPeca.add(item);
        item.setOrdemServico(this);
    }

    public void removerItemPeca(ItemPeca item) {
        itensPeca.remove(item);
        item.setOrdemServico(null);
    }

    public void adicionarItemServico(ItemServico item) {
        itensServico.add(item);
        item.setOrdemServico(this);
    }

    public void removerItemServico(ItemServico item) {
        itensServico.remove(item);
        item.setOrdemServico(null);
    }

    public void recalcularTotal() {
        BigDecimal totalPecas = (itensPeca == null || itensPeca.isEmpty())
                ? BigDecimal.ZERO
                : itensPeca.stream()
                    .map(item -> {
                        if (item.getSubtotal() != null) {
                            return item.getSubtotal();
                        }
                        if (item.getValorUnitario() != null && item.getQuantidade() != null) {
                            return item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalServicos = (itensServico == null || itensServico.isEmpty())
                ? BigDecimal.ZERO
                : itensServico.stream()
                    .map(item -> {
                        if (item.getSubtotal() != null) {
                            return BigDecimal.valueOf(item.getSubtotal());
                        }
                        if (item.getValorUnitario() != null && item.getQuantidade() != null) {
                            return BigDecimal.valueOf(item.getValorUnitario() * item.getQuantidade());
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = totalPecas.add(totalServicos);
    }

    public BigDecimal getTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }
}
