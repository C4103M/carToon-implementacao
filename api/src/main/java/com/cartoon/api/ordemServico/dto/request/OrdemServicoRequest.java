package com.cartoon.api.ordemServico.dto.request;

import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.veiculo.Veiculo;
import jakarta.validation.constraints.NotNull;

public record OrdemServicoRequest(
         @NotNull Integer veiculoId,
         @NotNull Integer oficinaId,
         @NotNull Integer mecanicoId,
         String descricao
) {
}
