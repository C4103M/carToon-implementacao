package com.cartoon.api.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cartoon.api.cliente.model.Cliente;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    boolean existsByCpf(String cpf);

    List<Cliente> findAllByAtivoTrue();

    List<Cliente> findAllByAtivoFalse();

    List<Cliente> findAllByOficinaId(Integer oficinaId);

    List<Cliente> findAllByOficinaIdAndAtivoTrue(Integer oficinaId);

    List<Cliente> findAllByOficinaIdAndAtivoFalse(Integer oficinaId);
}
