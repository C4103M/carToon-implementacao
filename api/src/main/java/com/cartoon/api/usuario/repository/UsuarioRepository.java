package com.cartoon.api.usuario.repository;

import com.cartoon.api.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findAllByAtivoTrue();

    List<Usuario> findAllByAtivoTrueAndOficinaId(Integer oficinaId);

    List<Usuario> findAllByOficinaId(Integer oficinaId);

    List<Usuario> findAllByAtivo(Boolean ativo);

    List<Usuario> findAllByAtivoAndOficinaId(Boolean ativo, Integer oficinaId);
}
