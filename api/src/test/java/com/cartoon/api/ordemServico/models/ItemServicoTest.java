package com.cartoon.api.ordemServico.models;

import com.cartoon.api.servico.models.Servico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ItemServicoTest {

    @Test
    @DisplayName("calcSubtotal - Deve calcular subtotal corretamente multiplicando quantidade por valorUnitario BigDecimal")
    void calcSubtotal_DeveCalcularSubtotalCorretamente() {
        ItemServico item = new ItemServico();
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("150.00"));

        item.calcSubtotal();

        assertNotNull(item.getSubtotal());
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("calcSubtotal - Deve calcular subtotal preservando precisão decimal com BigDecimal")
    void calcSubtotal_DevePreservarPrecisaoDecimal() {
        ItemServico item = new ItemServico();
        item.setQuantidade(3);
        item.setValorUnitario(new BigDecimal("49.99"));

        item.calcSubtotal();

        assertNotNull(item.getSubtotal());
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("149.97"));
    }

    @Test
    @DisplayName("calcSubtotal - Deve calcular subtotal quando quantidade for 1")
    void calcSubtotal_QuandoQuantidadeForUm_DeveSerIgualAoValorUnitario() {
        ItemServico item = new ItemServico();
        item.setQuantidade(1);
        item.setValorUnitario(new BigDecimal("85.50"));

        item.calcSubtotal();

        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("85.50"));
    }

    @Test
    @DisplayName("calcSubtotal - Deve calcular subtotal zero quando quantidade for 0")
    void calcSubtotal_QuandoQuantidadeForZero_DeveSerZero() {
        ItemServico item = new ItemServico();
        item.setQuantidade(0);
        item.setValorUnitario(new BigDecimal("100.00"));

        item.calcSubtotal();

        assertThat(item.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("ItemServico - Deve atribuir e recuperar campos monetários como BigDecimal e quantidadeHoras")
    void deveAtribuirERecuperarCamposComoBigDecimal() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Alinhamento");
        servico.setQuantidadeHoras(1.5);

        ItemServico item = new ItemServico();
        item.setId(10);
        item.setServico(servico);
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("120.50"));
        item.setSubtotal(new BigDecimal("241.00"));
        item.setQuantidadeHoras(1.5);
        item.setPesoServico(new BigDecimal("0.5"));

        assertEquals(10, item.getId());
        assertEquals(servico, item.getServico());
        assertEquals(2, item.getQuantidade());
        assertThat(item.getValorUnitario()).isEqualByComparingTo(new BigDecimal("120.50"));
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("241.00"));
        assertEquals(1.5, item.getQuantidadeHoras());
        assertThat(item.getPesoServico()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("ItemServico - Deve atribuir peso padrão 0.5 quando setado com nulo")
    void deveAtribuirPesoPadraoQuandoNulo() {
        ItemServico item = new ItemServico();
        item.setPesoServico(null);
        assertThat(item.getPesoServico()).isEqualByComparingTo(new BigDecimal("0.5"));
    }
}
