package com.cartoon.api.servico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {
    Page<Servico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
