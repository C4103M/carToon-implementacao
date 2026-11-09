package com.cartoon.api.dashboard.dto.projecao;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.time.LocalDate;

/** Data de abertura e status de uma OS, para volume e distribuição por status. */
public record OsAberturaProjecao(LocalDate dataOrcamento, StatusServico status) {
}
