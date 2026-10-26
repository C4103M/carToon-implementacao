package com.cartoon.api.cliente.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cartoon.api.cliente.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    
    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByOficinaIdAndCpf(Integer oficinaId, String cpf);

    boolean existsByOficinaIdAndCpfAndIdNot(Integer oficinaId, String cpf, Integer id);

    Optional<Cliente> findByOficinaIdAndCpf(Integer oficinaId, String cpf);

    List<Cliente> findAllByAtivoTrue();

    List<Cliente> findAllByAtivoFalse();

    List<Cliente> findAllByOficinaId(Integer oficinaId);

    List<Cliente> findAllByOficinaIdAndAtivoTrue(Integer oficinaId);

    List<Cliente> findAllByOficinaIdAndAtivoFalse(Integer oficinaId);
}
