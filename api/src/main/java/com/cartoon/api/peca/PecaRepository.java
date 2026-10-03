package com.cartoon.api.peca;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PecaRepository extends JpaRepository<Peca, Integer> {
    Page<Peca> findByNomeContainingIgnoreCaseOrFabricanteContainingIgnoreCase(String nome, String fabricante, Pageable pageable);
    Page<Peca> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Peca> findByFabricanteContainingIgnoreCase(String fabricante, Pageable pageable);
    Page<Peca> findByNomeContainingIgnoreCaseAndFabricanteContainingIgnoreCase(String nome, String fabricante, Pageable pageable);
}
