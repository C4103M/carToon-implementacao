package com.cartoon.api.ordemServico.models;

import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.veiculo.model.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Version
    Long versao;

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

    @Column(name = "desconto")
    @Enumerated(EnumType.STRING)
    Desconto desconto = Desconto.SEM_DESCONTO;

    @Column(name = "valor_bruto")
    BigDecimal valorBruto = BigDecimal.ZERO;

    @Column(name = "valor_desconto")
    BigDecimal valorDesconto = BigDecimal.ZERO;

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
                            return item.getSubtotal();
                        }
                        if (item.getValorUnitario() != null && item.getQuantidade() != null) {
                            return item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.valorBruto = totalPecas.add(totalServicos).setScale(2, RoundingMode.HALF_UP);

        Desconto desc = this.desconto != null ? this.desconto : Desconto.SEM_DESCONTO;
        this.valorDesconto = desc.calcularValorDesconto(this.valorBruto);
        this.total = this.valorBruto.subtract(this.valorDesconto).setScale(2, RoundingMode.HALF_UP);
    }

    public Desconto getDesconto() {
        return desconto != null ? desconto : Desconto.SEM_DESCONTO;
    }

    public BigDecimal getValorBruto() {
        return valorBruto != null ? valorBruto : (total != null ? total : BigDecimal.ZERO);
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto != null ? valorDesconto : BigDecimal.ZERO;
    }

    public BigDecimal getTotal() {
        return total != null ? total : BigDecimal.ZERO;
    }
}
