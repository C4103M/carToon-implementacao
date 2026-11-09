package com.cartoon.api.usuario.repository;

import com.cartoon.api.usuario.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Carrega o usuário já com a oficina. Usado no filtro JWT, que roda fora de uma
     * sessão do Hibernate e precisa ler {@code oficina.ativo} sem lazy-loading.
     */
    @EntityGraph(attributePaths = "oficina")
    Optional<Usuario> findComOficinaById(Integer id);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    List<Usuario> findAllByAtivoTrue();

    List<Usuario> findAllByAtivoTrueAndOficinaId(Integer oficinaId);

    List<Usuario> findAllByOficinaId(Integer oficinaId);

    List<Usuario> findAllByAtivo(Boolean ativo);

    List<Usuario> findAllByAtivoAndOficinaId(Boolean ativo, Integer oficinaId);
}
