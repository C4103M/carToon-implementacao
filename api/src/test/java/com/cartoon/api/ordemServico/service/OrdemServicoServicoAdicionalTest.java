package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.ItemServicoResponse;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.service.VeiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServicoAdicionalTest {

    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private VeiculoService veiculoService;
    @Mock private UsuarioService usuarioService;
    @Mock private OficinaService oficinaService;
    @Mock private PecaService pecaService;
    @Mock private ServicoService servicoService;
    @Spy private CalculadoraOrcamento calculadora = new CalculadoraOrcamento();

    @InjectMocks
    private OrdemServicoService service;

    private OrdemServico ordem;
    private Veiculo veiculo;
    private Servico servico;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo();
        veiculo.setValorFipe(50000.0);

        ordem = new OrdemServico();
        ordem.setVeiculo(veiculo);

        servico = new Servico();
        servico.setValorBase(new BigDecimal("150.00"));
    }

    private void prepararBusca() {
        when(ordemServicoRepository.findById(1)).thenReturn(Optional.of(ordem));
        when(servicoService.buscarEntidade(1)).thenReturn(servico);
    }

    @Test
    @DisplayName("adicionarServico - Deve somar 1% do valor FIPE ao valor base do serviço")
    void adicionarServico_DeveAplicarUmPorCentoDoFipe() {
        prepararBusca();

        OrdemServicoResponse resposta = service.adicionarServico(1, new ItemServicoRequest(1, 1));

        ItemServicoResponse item = resposta.itensServico().get(0);
        assertEquals(650.0, item.valorUnitario());
        assertEquals(650.0, item.subtotal());
    }

    @Test
    @DisplayName("adicionarServico - O adicional vale para cada unidade do serviço")
    void adicionarServico_QuantidadeDeveMultiplicarValorComAdicional() {
        prepararBusca();

        OrdemServicoResponse resposta = service.adicionarServico(1, new ItemServicoRequest(1, 2));

        ItemServicoResponse item = resposta.itensServico().get(0);
        assertEquals(650.0, item.valorUnitario());
        assertEquals(1300.0, item.subtotal());
    }

    @Test
    @DisplayName("adicionarServico - Veículo mais caro deve gerar serviço mais caro")
    void adicionarServico_VeiculoMaisCaroDeveCustarMais() {
        veiculo.setValorFipe(300000.0);
        prepararBusca();

        OrdemServicoResponse resposta = service.adicionarServico(1, new ItemServicoRequest(1, 1));

        assertEquals(3150.0, resposta.itensServico().get(0).valorUnitario());
    }

    @Test
    @DisplayName("adicionarServico - Deve recusar quando o veículo não tem valor FIPE")
    void adicionarServico_SemFipe_DeveLancarRegraDeNegocio() {
        veiculo.setValorFipe(null);
        prepararBusca();

        assertThrows(RegraDeNegocioException.class,
                () -> service.adicionarServico(1, new ItemServicoRequest(1, 1)));
        assertTrue(ordem.getItensServico().isEmpty());
    }

    @Test
    @DisplayName("adicionarServico - Não deve alterar ordem finalizada")
    void adicionarServico_OrdemFinalizada_DeveLancarConflito() {
        ordem.setStatusServico(StatusServico.FINALIZADO);
        prepararBusca();

        assertThrows(ConflitoException.class,
                () -> service.adicionarServico(1, new ItemServicoRequest(1, 1)));
    }
}