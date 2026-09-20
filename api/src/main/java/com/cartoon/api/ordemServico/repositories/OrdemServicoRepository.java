package com.cartoon.api.ordemServico.repositories;

import com.cartoon.api.ordemServico.models.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer>, JpaSpecificationExecutor<OrdemServico> {
    @EntityGraph(attributePaths = {"veiculo", "mecanico"})
    Page<OrdemServico> findAll(Specification<OrdemServico> spec, Pageable pageable);
}
