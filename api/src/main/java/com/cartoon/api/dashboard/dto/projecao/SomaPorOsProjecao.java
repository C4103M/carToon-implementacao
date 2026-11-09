package com.cartoon.api.dashboard.dto.projecao;

import java.math.BigDecimal;

/** Soma dos subtotais de itens (peças ou serviços) de uma OS. */
public record SomaPorOsProjecao(Integer ordemServicoId, BigDecimal valor) {
}
