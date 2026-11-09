package com.cartoon.api.oficina.repository;

import com.cartoon.api.oficina.model.Oficina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OficinaRepository extends JpaRepository<Oficina, Integer> {
    List<Oficina> findAllByAtivoTrue();
    List<Oficina> findAllByAtivo(Boolean ativo);
    Optional<Oficina> findByEmail(String email);
    boolean existsByEmail(String email);
}
