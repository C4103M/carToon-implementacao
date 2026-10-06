package com.cartoon.api.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cartoon.api.cliente.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    boolean existsByCpf(String cpf);
    
}