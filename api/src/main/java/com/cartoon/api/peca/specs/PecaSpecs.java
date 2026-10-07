package com.cartoon.api.peca.specs;

import com.cartoon.api.peca.dto.PecaFiltro;
import com.cartoon.api.peca.models.Peca;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PecaSpecs {

    private PecaSpecs() {}

    public static Specification<Peca> montarFiltros(PecaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (filtro != null) {
                if (filtro.busca() != null && !filtro.busca().isBlank()) {
                    String termo = "%" + filtro.busca().trim().toLowerCase() + "%";
                    condicoes.add(cb.or(
                            cb.like(cb.lower(root.get("nome")), termo),
                            cb.like(cb.lower(root.get("fabricante")), termo)
                    ));
                }
                if (filtro.nome() != null && !filtro.nome().isBlank()) {
                    String termo = "%" + filtro.nome().trim().toLowerCase() + "%";
                    condicoes.add(cb.like(cb.lower(root.get("nome")), termo));
                }
                if (filtro.fabricante() != null && !filtro.fabricante().isBlank()) {
                    String termo = "%" + filtro.fabricante().trim().toLowerCase() + "%";
                    condicoes.add(cb.like(cb.lower(root.get("fabricante")), termo));
                }
            }

            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
