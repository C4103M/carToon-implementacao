package com.cartoon.api.usuario.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findAllByAtivoTrue();

    List<Usuario> findAllByAtivoTrueAndOficinaId(Integer oficinaId);
}
