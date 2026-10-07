package com.cartoon.api.veiculo.specs;

import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VeiculoSpecs {

    private VeiculoSpecs() {}

    public static Specification<Veiculo> montarFiltros(VeiculoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (filtro.clienteId() != null) {
                condicoes.add(cb.equal(root.get("cliente").get("id"), filtro.clienteId()));
            }
            if (filtro.placa() != null && !filtro.placa().isBlank()) {
                condicoes.add(cb.like(cb.upper(root.get("placa")), "%" + filtro.placa().trim().toUpperCase() + "%"));
            }
            if (filtro.modelo() != null && !filtro.modelo().isBlank()) {
                condicoes.add(cb.like(cb.upper(root.get("modelo")), "%" + filtro.modelo().trim().toUpperCase() + "%"));
            }
            if (filtro.montadora() != null && !filtro.montadora().isBlank()) {
                condicoes.add(cb.like(cb.upper(root.get("montadora")), "%" + filtro.montadora().trim().toUpperCase() + "%"));
            }
            if (filtro.ativo() != null) {
                condicoes.add(cb.equal(root.get("ativo"), filtro.ativo()));
            }

            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
