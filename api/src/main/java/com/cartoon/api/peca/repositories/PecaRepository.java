package com.cartoon.api.peca.repositories;

import com.cartoon.api.peca.models.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PecaRepository extends JpaRepository<Peca, Integer>, JpaSpecificationExecutor<Peca> {
    Page<Peca> findByNomeContainingIgnoreCaseOrFabricanteContainingIgnoreCase(String nome, String fabricante, Pageable pageable);
    Page<Peca> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Peca> findByFabricanteContainingIgnoreCase(String fabricante, Pageable pageable);
    Page<Peca> findByNomeContainingIgnoreCaseAndFabricanteContainingIgnoreCase(String nome, String fabricante, Pageable pageable);
}
