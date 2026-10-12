package com.cartoon.api.veiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer>, JpaSpecificationExecutor<Veiculo> {

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Integer id);

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    @EntityGraph(attributePaths = {"cliente"})
    Optional<Veiculo> findComClienteById(Integer id);

    @EntityGraph(attributePaths = {"cliente"})
    Optional<Veiculo> findComClienteByPlacaIgnoreCase(String placa);

    List<Veiculo> findByClienteId(Integer clienteId);

    Page<Veiculo> findByClienteId(Integer clienteId, Pageable pageable);

    Page<Veiculo> findByAtivoTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"cliente"})
    Page<Veiculo> findAll(Specification<Veiculo> spec, Pageable pageable);
}
