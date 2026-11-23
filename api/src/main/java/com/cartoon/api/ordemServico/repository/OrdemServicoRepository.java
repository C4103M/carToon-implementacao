package com.cartoon.api.ordemServico.repository;

import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Integer>, JpaSpecificationExecutor<OrdemServico> {

    @EntityGraph(attributePaths = {"veiculo", "mecanico"})
    Page<OrdemServico> findAll(Specification<OrdemServico> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    List<OrdemServico> findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(String placa);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    Page<OrdemServico> findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc(String placa, Pageable pageable);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    List<OrdemServico> findByVeiculoIdOrderByDataOrcamentoDescIdDesc(Integer veiculoId);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    List<OrdemServico> findByVeiculoIdAndOficinaIdOrderByDataOrcamentoDescIdDesc(Integer veiculoId, Integer oficinaId);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    Page<OrdemServico> findByVeiculoIdOrderByDataOrcamentoDescIdDesc(Integer veiculoId, Pageable pageable);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    Page<OrdemServico> findByVeiculoIdAndOficinaIdOrderByDataOrcamentoDescIdDesc(Integer veiculoId, Integer oficinaId, Pageable pageable);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    List<OrdemServico> findByVeiculoPlacaIgnoreCaseAndOficinaIdOrderByDataOrcamentoDescIdDesc(String placa, Integer oficinaId);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    Page<OrdemServico> findByVeiculoPlacaIgnoreCaseAndOficinaIdOrderByDataOrcamentoDescIdDesc(String placa, Integer oficinaId, Pageable pageable);

    @EntityGraph(attributePaths = {"veiculo", "mecanico", "oficina", "itensPeca", "itensServico"})
    Optional<OrdemServico> findDetalhadaById(Integer id);

    boolean existsByOficinaIdAndStatusServicoIn(Integer oficinaId, Collection<StatusServico> status);

    boolean existsByMecanicoIdAndStatusServicoIn(Integer mecanicoId, Collection<StatusServico> status);

    boolean existsByVeiculoIdAndStatusServicoIn(Integer veiculoId, Collection<StatusServico> status);

    boolean existsByVeiculoClienteIdAndStatusServicoIn(Integer clienteId, Collection<StatusServico> status);
}
