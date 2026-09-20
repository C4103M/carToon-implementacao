package com.cartoon.api.ordemServico.specs;

import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.models.OrdemServico;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrdemServicoSpecs {
    private OrdemServicoSpecs() {}

    public static Specification<OrdemServico> montarFiltros(OrdemServicoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.oficinaId() != null)
                condicoes.add(cb.equal(root.get("oficina").get("id"), filtro.oficinaId()));
            if (filtro.veiculoId() != null)
                condicoes.add(cb.equal(root.get("veiculo").get("id"), filtro.veiculoId()));
            if (filtro.mecanicoId() != null)
                condicoes.add(cb.equal(root.get("mecanico").get("id"), filtro.mecanicoId()));
            if (filtro.statusServico() != null)
                condicoes.add(cb.equal(root.get("statusServico"), filtro.statusServico()));
            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}