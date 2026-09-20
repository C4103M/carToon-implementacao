package com.cartoon.api.ordemServico.repositories;

import com.cartoon.api.ordemServico.models.ItemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemServicoRepository extends JpaRepository<ItemServico, Integer> {
}
