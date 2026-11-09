package com.cartoon.api.dashboard.dto.projecao;

import com.cartoon.api.ordemServico.models.StatusServico;

import java.time.LocalDate;

public record OsAberturaProjecao(LocalDate dataOrcamento, StatusServico status) {
}
