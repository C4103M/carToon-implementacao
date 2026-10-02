package com.cartoon.api.oficina.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OficinaRepository extends JpaRepository<Oficina, Integer> {
    List<Oficina> findAllByAtivoTrue();
}
