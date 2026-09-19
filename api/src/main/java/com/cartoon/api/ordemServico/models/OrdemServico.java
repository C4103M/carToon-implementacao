package com.cartoon.api.ordemServico.models;

import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Column(name = "descricao_problema", nullable = false)
    String descricaoProblema;

    @Column(name = "data_orcamento")
    LocalDate dataOrcamento;
    @Column(name = "data_rejeicao")
    LocalDate dataRejeicao;
    @Column(name = "data_inicio")
    LocalDate dataInicio;
    @Column(name = "data_finalizacao")
    LocalDate dataFinalizacao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    StatusServico statusServico;

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

}
