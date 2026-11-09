package com.cartoon.api.servico.repository;

import com.cartoon.api.servico.models.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    Page<Servico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Servico> findByOficinaId(Integer oficinaId, Pageable pageable);
    Page<Servico> findByOficinaIdAndNomeContainingIgnoreCase(Integer oficinaId, String nome, Pageable pageable);
    Page<Servico> findByAtivoTrue(Pageable pageable);
    Page<Servico> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);
    Page<Servico> findByOficinaIdAndAtivoTrue(Integer oficinaId, Pageable pageable);
    Page<Servico> findByOficinaIdAndNomeContainingIgnoreCaseAndAtivoTrue(Integer oficinaId, String nome, Pageable pageable);
}
