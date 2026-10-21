package com.cartoon.api.ordemServico.mapper;

import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.servico.models.Servico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoMapperTest {

    @Test
    @DisplayName("paraItemServicoResponse - Deve mapear ItemServico com subtotal preexistente para ItemServicoResponse com BigDecimal")
    void paraItemServicoResponse_ComSubtotalPreexistente_DeveMapearParaResponse() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");

        ItemServico item = new ItemServico();
        item.setId(5);
        item.setServico(servico);
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("100.00"));
        item.setSubtotal(new BigDecimal("200.00"));
        item.setTempo(LocalTime.of(0, 45));

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertEquals(5, response.id());
        assertEquals(1, response.servicoId());
        assertEquals("Troca de Óleo", response.servicoNome());
        assertEquals(2, response.quantidade());
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertEquals(LocalTime.of(0, 45), response.tempo());
    }

    @Test
    @DisplayName("paraItemServicoResponse - Quando subtotal for nulo, deve calcular fallback usando BigDecimal")
    void paraItemServicoResponse_QuandoSubtotalNulo_DeveCalcularSubtotalComBigDecimal() {
        Servico servico = new Servico();
        servico.setId(2);
        servico.setNome("Balanceamento");

        ItemServico item = new ItemServico();
        item.setId(10);
        item.setServico(servico);
        item.setQuantidade(4);
        item.setValorUnitario(new BigDecimal("35.50"));
        item.setSubtotal(null);
        item.setTempo(LocalTime.of(1, 0));

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertNotNull(response.subtotal());
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("142.00"));
    }

    @Test
    @DisplayName("paraItemServicoResponse - Deve preservar precisão decimal sem problemas de ponto flutuante")
    void paraItemServicoResponse_DevePreservarPrecisaoDecimal() {
        ItemServico item = new ItemServico();
        item.setId(15);
        item.setQuantidade(3);
        item.setValorUnitario(new BigDecimal("33.33"));
        item.setSubtotal(null);

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("paraItemServicoResponse - Quando servico for nulo, deve mapear servicoId e servicoNome como nulos")
    void paraItemServicoResponse_QuandoServicoNulo_DeveMapearCamposServicoComoNulo() {
        ItemServico item = new ItemServico();
        item.setId(20);
        item.setServico(null);
        item.setQuantidade(1);
        item.setValorUnitario(new BigDecimal("50.00"));
        item.setSubtotal(new BigDecimal("50.00"));

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertNull(response.servicoId());
        assertNull(response.servicoNome());
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("paraItemServico - Deve criar ItemServico com valorUnitario como BigDecimal e calcular subtotal")
    void paraItemServico_ComServicoValido_DeveMapearComBigDecimalECalcularSubtotal() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");
        servico.setValorBase(new BigDecimal("150.00"));
        servico.setTempoEstimado(LocalTime.of(1, 0));

        com.cartoon.api.ordemServico.dto.request.ItemServicoRequest request =
                new com.cartoon.api.ordemServico.dto.request.ItemServicoRequest(1, 2);

        ItemServico item = OrdemServicoMapper.paraItemServico(request, servico);

        assertNotNull(item);
        assertEquals(servico, item.getServico());
        assertEquals(2, item.getQuantidade());
        assertThat(item.getValorUnitario()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertNotNull(item.getSubtotal());
        assertThat(item.getSubtotal()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertEquals(LocalTime.of(1, 0), item.getTempo());
    }
}
