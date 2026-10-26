package com.cartoon.api.ordemServico.mapper;

import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.ordemServico.models.ItemPeca;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoMapperTest {

    @Test
    @DisplayName("paraItemServicoResponse - Deve mapear ItemServico com subtotal preexistente para ItemServicoResponse com BigDecimal")
    void paraItemServicoResponse_ComSubtotalPreexistente_DeveMapearParaResponse() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");
        servico.setQuantidadeHoras(0.75);

        ItemServico item = new ItemServico();
        item.setId(5);
        item.setServico(servico);
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("100.00"));
        item.setSubtotal(new BigDecimal("200.00"));
        item.setQuantidadeHoras(0.75);

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertEquals(5, response.id());
        assertEquals(1, response.servicoId());
        assertEquals("Troca de Óleo", response.servicoNome());
        assertEquals(2, response.quantidade());
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertEquals(0.75, response.quantidadeHoras());
    }

    @Test
    @DisplayName("paraItemServicoResponse - Quando subtotal for nulo, deve calcular fallback usando BigDecimal")
    void paraItemServicoResponse_QuandoSubtotalNulo_DeveCalcularSubtotalComBigDecimal() {
        Servico servico = new Servico();
        servico.setId(2);
        servico.setNome("Balanceamento");
        servico.setQuantidadeHoras(1.0);

        ItemServico item = new ItemServico();
        item.setId(10);
        item.setServico(servico);
        item.setQuantidade(4);
        item.setValorUnitario(new BigDecimal("35.50"));
        item.setSubtotal(null);
        item.setQuantidadeHoras(1.0);

        ItemServicoResponse response = OrdemServicoMapper.paraItemServicoResponse(item);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("35.50"));
        assertNotNull(response.subtotal());
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("142.00"));
        assertEquals(1.0, response.quantidadeHoras());
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
    @DisplayName("paraItemServico - Deve criar ItemServico com quantidadeHoras herdadas de Servico")
    void paraItemServico_ComServicoValido_DeveMapearQuantidadeHoras() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");
        servico.setQuantidadeHoras(1.0);

        com.cartoon.api.ordemServico.dto.request.ItemServicoRequest request =
                new com.cartoon.api.ordemServico.dto.request.ItemServicoRequest(1, 2);

        ItemServico item = OrdemServicoMapper.paraItemServico(request, servico);

        assertNotNull(item);
        assertEquals(servico, item.getServico());
        assertEquals(2, item.getQuantidade());
        assertEquals(1.0, item.getQuantidadeHoras());
    }

    @Test
    @DisplayName("paraItemPecaResponse - Deve discriminar valorBase (bruto), margem, valorUnitario (final) e subtotais")
    void paraItemPecaResponse_DeveDiscriminarValorBrutoEMargemEValorFinal() {
        Peca peca = new Peca();
        peca.setId(10);
        peca.setNome("Pastilha de Freio");
        peca.setFabricante("Bosch");
        peca.setValorBase(new BigDecimal("100.00"));

        ItemPeca item = new ItemPeca();
        item.setId(1);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorBase(new BigDecimal("100.00"));
        item.setMargem(new BigDecimal("1.10"));
        item.setValorUnitario(new BigDecimal("110.00"));
        item.setSubtotal(new BigDecimal("220.00"));

        com.cartoon.api.ordemServico.dto.response.ItemPecaResponse response = OrdemServicoMapper.paraItemPecaResponse(item);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(10, response.pecaId());
        assertEquals("Pastilha de Freio", response.pecaNome());
        assertEquals("Bosch", response.fabricante());
        assertEquals(2, response.quantidade());
        assertThat(response.valorBase()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.valorBruto()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.margem()).isEqualByComparingTo(new BigDecimal("1.10"));
        assertThat(response.valorUnitario()).isEqualByComparingTo(new BigDecimal("110.00"));
        assertThat(response.valorFinal()).isEqualByComparingTo(new BigDecimal("110.00"));
        assertThat(response.subtotalBruto()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("220.00"));
    }

    @Test
    @DisplayName("paraOrdemServicoResumo - Deve mapear totalPecasBruto e totalPecas discriminados e itensPeca")
    void paraOrdemServicoResumo_DeveMapearTotalPecasBrutoETotalPecas() {
        Peca peca = new Peca();
        peca.setId(10);
        peca.setNome("Amortecedor");
        peca.setValorBase(new BigDecimal("200.00"));

        ItemPeca item = new ItemPeca();
        item.setId(2);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorBase(new BigDecimal("200.00"));
        item.setMargem(new BigDecimal("1.20"));
        item.setValorUnitario(new BigDecimal("240.00"));
        item.setSubtotal(new BigDecimal("480.00"));

        com.cartoon.api.ordemServico.models.OrdemServico os = new com.cartoon.api.ordemServico.models.OrdemServico();
        os.setId(50);
        os.setItensPeca(java.util.List.of(item));
        os.setItensServico(java.util.List.of());
        os.setValorBruto(new BigDecimal("480.00"));
        os.setDesconto(com.cartoon.api.ordemServico.models.Desconto.SEM_DESCONTO);
        os.setValorDesconto(BigDecimal.ZERO);
        os.setTotal(new BigDecimal("480.00"));

        com.cartoon.api.ordemServico.dto.OrdemServicoResumo resumo = OrdemServicoMapper.paraOrdemServicoResumo(os);

        assertNotNull(resumo);
        assertThat(resumo.totalPecasBruto()).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat(resumo.totalPecas()).isEqualByComparingTo(new BigDecimal("480.00"));
        assertNotNull(resumo.itensPeca());
        assertEquals(1, resumo.itensPeca().size());
        assertThat(resumo.itensPeca().get(0).valorBruto()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(resumo.itensPeca().get(0).valorFinal()).isEqualByComparingTo(new BigDecimal("240.00"));
    }
}