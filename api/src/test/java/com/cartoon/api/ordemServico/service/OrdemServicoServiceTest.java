package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private OficinaService oficinaService;

    @Mock
    private PecaService pecaService;

    @Mock
    private ServicoService servicoService;

    @Spy
    private CalculadoraOrcamento calculadora = new CalculadoraOrcamento();

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    private Veiculo veiculo;
    private Oficina oficina;
    private Usuario mecanico;
    private OrdemServico ordemServico;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo();
        veiculo.setId(1);
        veiculo.setPlaca("ABC-1234");

        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Central");

        mecanico = new Usuario();
        mecanico.setId(1);
        mecanico.setNome("Carlos Mecanico");
        mecanico.setRole(Role.MECANICO);

        ordemServico = new OrdemServico();
        ordemServico.setId(10);
        ordemServico.setDescricao("Troca de óleo");
        ordemServico.setStatusServico(StatusServico.PENDENTE);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setOficina(oficina);
        ordemServico.setMecanico(mecanico);
        ordemServico.setItensPeca(new ArrayList<>());
        ordemServico.setTotal(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Salvar - Deve criar e retornar nova Ordem de Serviço com data de inicio como agora")
    void salvar_ComDadosValidos_DeveSalvarERetornarResponse() {
        OrdemServicoRequest request = new OrdemServicoRequest(1, 1, 1, "Troca de óleo");

        when(veiculoService.buscarEntidade(1)).thenReturn(veiculo);
        when(oficinaService.buscarEntidade(1)).thenReturn(oficina);
        when(usuarioService.buscarEntidade(1)).thenReturn(mecanico);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(inv -> {
            OrdemServico os = inv.getArgument(0);
            os.setId(10);
            return os;
        });

        OrdemServicoResponse response = ordemServicoService.salvar(request);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertEquals("Troca de óleo", response.descricao());
        assertEquals(StatusServico.PENDENTE, response.status());
        assertEquals("ABC-1234", response.placa());
        assertEquals("Carlos Mecanico", response.mecanicoNome());
        assertEquals(LocalDate.now(), response.dataOrcamento());

        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Buscar - Deve retornar OrdemServicoResponse quando a OS for encontrada")
    void buscar_QuandoExiste_DeveRetornarResponse() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.buscar(10);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertEquals("ABC-1234", response.placa());
        verify(ordemServicoRepository).findById(10);
    }

    @Test
    @DisplayName("Buscar - Deve lançar RecursoNaoEncontradoException quando OS não for encontrada")
    void buscar_QuandoNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.buscar(99));
    }

    @Test
    @DisplayName("Listar - Deve retornar página de resumos de OrdemServico")
    void listar_DeveRetornarPaginaDeResumos() {
        OrdemServicoFiltro filtro = new OrdemServicoFiltro(1, 1, 1, StatusServico.PENDENTE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(ordemServico), pageable, 1);

        when(ordemServicoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoResumo> resultado = ordemServicoService.listar(filtro, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(10, resultado.getContent().get(0).id());
        assertEquals("ABC-1234", resultado.getContent().get(0).placa());
    }

    @Test
    @DisplayName("Aceitar - Deve atualizar status para ACEITO")
    void aceitar_DeveAtualizarStatusParaAceito() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.aceitar(10);

        assertEquals(StatusServico.ACEITO, response.status());
        assertEquals(StatusServico.ACEITO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Iniciar - Deve atualizar status para EM_ANDAMENTO")
    void iniciar_DeveAtualizarStatusParaEmAndamento() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.iniciar(10);

        assertEquals(StatusServico.EM_ANDAMENTO, response.status());
        assertEquals(LocalDate.now(), response.dataInicio());
        assertEquals(StatusServico.EM_ANDAMENTO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Finalizar - Deve atualizar status para FINALIZADO")
    void finalizar_DeveAtualizarStatusParaFinalizado() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.finalizar(10);

        assertEquals(StatusServico.FINALIZADO, response.status());
        assertEquals(LocalDate.now(), response.dataFinalizacao());
        assertEquals(StatusServico.FINALIZADO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Rejeitar - Deve atualizar status para REJEITADO")
    void rejeitar_DeveAtualizarStatusParaRejeitado() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.rejeitar(10);

        assertEquals(StatusServico.REJEITADO, response.status());
        assertEquals(LocalDate.now(), response.dataRejeicao());
        assertEquals(StatusServico.REJEITADO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Adicionar Peça - Deve adicionar peça à ordem não finalizada")
    void adicionarPeca_QuandoOrdemNaoFinalizada_DeveAdicionarPeca() {
        ItemPecaRequest request = new ItemPecaRequest(5, 2);
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Filtro de Óleo");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(peca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, request);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensPeca().size());
        assertEquals("Filtro de Óleo", ordemServico.getItensPeca().get(0).getPeca().getNome());
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Adicionar Peça - Deve lançar ConflitoException quando ordem estiver finalizada")
    void adicionarPeca_QuandoOrdemFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        ItemPecaRequest request = new ItemPecaRequest(5, 2);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(new Peca());

        assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarPeca(10, request));
    }

    @Test
    @DisplayName("Remover Peça - Deve remover peça existente de ordem não finalizada")
    void removerPeca_QuandoExiste_DeveRemoverPeca() {
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Filtro de Óleo");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        ItemPeca item = new ItemPeca();
        item.setId(100);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorUnitario(BigDecimal.valueOf(50.0));
        ordemServico.adicionarItemPeca(item);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 100);

        assertNotNull(response);
        assertTrue(ordemServico.getItensPeca().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.total()));
        assertEquals(0, BigDecimal.ZERO.compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Remover Peça - Deve atualizar total quando ainda restam itens na ordem")
    void removerPeca_QuandoAindaRestamItens_DeveRecalcularTotal() {
        Peca peca1 = new Peca();
        peca1.setId(1);
        peca1.setNome("Filtro de Óleo");
        peca1.setValorBase(BigDecimal.valueOf(50.0));

        ItemPeca item1 = new ItemPeca();
        item1.setId(101);
        item1.setPeca(peca1);
        item1.setQuantidade(2);
        item1.setValorUnitario(BigDecimal.valueOf(50.0));
        item1.calcSubtotal();
        ordemServico.adicionarItemPeca(item1);

        Peca peca2 = new Peca();
        peca2.setId(2);
        peca2.setNome("Pastilha de Freio");
        peca2.setValorBase(BigDecimal.valueOf(80.0));

        ItemPeca item2 = new ItemPeca();
        item2.setId(102);
        item2.setPeca(peca2);
        item2.setQuantidade(1);
        item2.setValorUnitario(BigDecimal.valueOf(80.0));
        item2.calcSubtotal();
        ordemServico.adicionarItemPeca(item2);

        ordemServico.recalcularTotal();
        assertEquals(0, BigDecimal.valueOf(180.0).compareTo(ordemServico.getTotal()));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 101);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensPeca().size());
        assertEquals(0, BigDecimal.valueOf(80.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(80.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Recalcular Total - Deve somar subtotais de itensPeca e itensServico vinculados")
    void recalcularTotal_ComItensPecaEServico_DeveSomarAmbos() {
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setQuantidade(2);
        itemPeca.setValorUnitario(BigDecimal.valueOf(60.0));
        itemPeca.calcSubtotal();
        ordemServico.adicionarItemPeca(itemPeca);

        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(150.0);
        itemServico.setSubtotal(150.0);
        ordemServico.adicionarItemServico(itemServico);

        ordemServico.recalcularTotal();

        assertEquals(0, BigDecimal.valueOf(270.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Recalcular Total - Ordem sem itens deve ter total como BigDecimal.ZERO")
    void recalcularTotal_SemItens_DeveSerBigDecimalZero() {
        ordemServico.getItensPeca().clear();
        ordemServico.getItensServico().clear();
        ordemServico.recalcularTotal();

        assertEquals(0, BigDecimal.ZERO.compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Adicionar Peça - Deve acumular valor total quando a ordem já possui outras peças")
    void adicionarPeca_QuandoJaPossuiPecas_DeveAcumularValorTotal() {
        Peca pecaExistente = new Peca();
        pecaExistente.setId(1);
        pecaExistente.setNome("Óleo");
        pecaExistente.setValorBase(BigDecimal.valueOf(40.0));

        ItemPeca itemExistente = new ItemPeca();
        itemExistente.setId(101);
        itemExistente.setPeca(pecaExistente);
        itemExistente.setQuantidade(1);
        itemExistente.setValorUnitario(BigDecimal.valueOf(40.0));
        itemExistente.calcSubtotal();
        ordemServico.adicionarItemPeca(itemExistente);
        ordemServico.recalcularTotal();

        ItemPecaRequest novaPecaRequest = new ItemPecaRequest(2, 2);
        Peca novaPeca = new Peca();
        novaPeca.setId(2);
        novaPeca.setNome("Vela de Ignição");
        novaPeca.setValorBase(BigDecimal.valueOf(30.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(2)).thenReturn(novaPeca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, novaPecaRequest);

        assertNotNull(response);
        assertEquals(2, ordemServico.getItensPeca().size());
        // 40.0 + (30.0 * 2) = 100.0
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Adicionar Peça - Deve somar peça nova ao total quando a ordem já possui itens de serviço")
    void adicionarPeca_QuandoJaPossuiServicos_DeveSomarPecaAosServicos() {
        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(120.0);
        itemServico.setSubtotal(120.0);
        ordemServico.adicionarItemServico(itemServico);
        ordemServico.recalcularTotal();

        ItemPecaRequest request = new ItemPecaRequest(5, 1);
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Correia Dentada");
        peca.setValorBase(BigDecimal.valueOf(80.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(peca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, request);

        assertNotNull(response);
        // 120.0 (serviço) + 80.0 (peça) = 200.0
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Remover Peça - Deve manter valor dos serviços quando restar apenas itens de serviço na ordem")
    void removerPeca_QuandoRestarApenasServicos_TotalDeveSerApenasServicos() {
        Peca peca = new Peca();
        peca.setId(1);
        peca.setNome("Filtro");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(100);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(1);
        itemPeca.setValorUnitario(BigDecimal.valueOf(50.0));
        itemPeca.calcSubtotal();
        ordemServico.adicionarItemPeca(itemPeca);

        ItemServico itemServico = new ItemServico();
        itemServico.setId(200);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(150.0);
        itemServico.setSubtotal(150.0);
        ordemServico.adicionarItemServico(itemServico);

        ordemServico.recalcularTotal();
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(ordemServico.getTotal()));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 100);

        assertNotNull(response);
        assertTrue(ordemServico.getItensPeca().isEmpty());
        assertEquals(1, ordemServico.getItensServico().size());
        // Deve restar apenas os 150.0 do serviço
        assertEquals(0, BigDecimal.valueOf(150.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(150.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Recalcular Total - Deve manter precisão correta com valores decimais fracionados")
    void recalcularTotal_ComValoresDecimaisFracionados_DeveManterPrecisao() {
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setQuantidade(2);
        itemPeca.setValorUnitario(new BigDecimal("12.35"));
        itemPeca.calcSubtotal(); // 24.70
        ordemServico.adicionarItemPeca(itemPeca);

        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(45.30);
        itemServico.setSubtotal(45.30);
        ordemServico.adicionarItemServico(itemServico);

        ordemServico.recalcularTotal();

        // 24.70 + 45.30 = 70.00
        assertEquals(0, new BigDecimal("70.00").compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Recalcular Total - Deve calcular subtotal dinamicamente se o campo subtotal estiver nulo")
    void recalcularTotal_ComSubtotalNuloMasValorEQuantidadeDefinidos_DeveCalcularSubtotal() {
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setQuantidade(3);
        itemPeca.setValorUnitario(BigDecimal.valueOf(30.0));
        itemPeca.setSubtotal(null); // propositalmente nulo
        ordemServico.adicionarItemPeca(itemPeca);

        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setQuantidade(2);
        itemServico.setValorUnitario(50.0);
        itemServico.setSubtotal(null); // propositalmente nulo
        ordemServico.adicionarItemServico(itemServico);

        ordemServico.recalcularTotal();

        // (3 * 30.0 = 90.0) + (2 * 50.0 = 100.0) = 190.0
        assertEquals(0, BigDecimal.valueOf(190.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Buscar - OrdemServicoResponse deve refletir valor total recalculado da OS")
    void buscar_DeveRetornarResponseComValorTotalAtualizado() {
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        Peca peca = new Peca();
        peca.setId(1);
        peca.setNome("Pneu");
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(4);
        itemPeca.setValorUnitario(BigDecimal.valueOf(250.0));
        itemPeca.calcSubtotal(); // 1000.0
        ordemServico.adicionarItemPeca(itemPeca);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.buscar(10);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(1000.0).compareTo(response.total()));
    }

    @Test
    @DisplayName("Listar - OrdemServicoResumo deve refletir valor total recalculado da OS")
    void listar_DeveRetornarResumoComValorTotalAtualizado() {
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        Peca peca = new Peca();
        peca.setId(1);
        peca.setNome("Amortecedor");
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(2);
        itemPeca.setValorUnitario(BigDecimal.valueOf(200.0));
        itemPeca.calcSubtotal(); // 400.0
        ordemServico.adicionarItemPeca(itemPeca);
        ordemServico.recalcularTotal();

        OrdemServicoFiltro filtro = new OrdemServicoFiltro(1, 1, 1, StatusServico.PENDENTE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(ordemServico), pageable, 1);

        when(ordemServicoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoResumo> resultado = ordemServicoService.listar(filtro, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(0, BigDecimal.valueOf(400.0).compareTo(resultado.getContent().get(0).valorTotal()));
    }

    @Test
    @DisplayName("Remover Peça - Deve lançar RecursoNaoEncontradoException quando item de peça não existir")
    void removerPeca_QuandoItemNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.removerPeca(10, 999));
    }

    @Test
    @DisplayName("Excluir - Deve excluir ordem quando não estiver finalizada")
    void excluir_QuandoNaoFinalizada_DeveExcluir() throws Exception {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        ordemServicoService.excluir(10);

        verify(ordemServicoRepository).delete(ordemServico);
    }

    @Test
    @DisplayName("Excluir - Deve lançar ConflitoException ao tentar excluir ordem finalizada")
    void excluir_QuandoFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(ConflitoException.class, () -> ordemServicoService.excluir(10));
        verify(ordemServicoRepository, never()).delete(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Adicionar Serviço - Deve adicionar serviço em ordem não finalizada com 1% do valor FIPE")
    void adicionarServico_QuandoOrdemNaoFinalizada_DeveAdicionarServico() {
        veiculo.setValorFipe(50000.0);
        ItemServicoRequest request = new ItemServicoRequest(3, 1);
        Servico servico = new Servico();
        servico.setId(3);
        servico.setNome("Alinhamento e Balanceamento");
        servico.setValorBase(BigDecimal.valueOf(120.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarEntidade(3)).thenReturn(servico);

        OrdemServicoResponse response = ordemServicoService.adicionarServico(10, request);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensServico().size());
        ItemServico item = ordemServico.getItensServico().get(0);
        assertEquals("Alinhamento e Balanceamento", item.getServico().getNome());
        assertEquals(620.0, item.getValorUnitario());
        assertEquals(620.0, item.getSubtotal());
    }

    @Test
    @DisplayName("Adicionar Serviço - Deve lançar ConflitoException quando ordem estiver finalizada")
    void adicionarServico_QuandoOrdemFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        ItemServicoRequest request = new ItemServicoRequest(3, 1);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarEntidade(3)).thenReturn(new Servico());

        assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarServico(10, request));
    }

    @Test
    @DisplayName("Remover Serviço - Deve remover serviço existente de ordem não finalizada")
    void removerServico_QuandoExiste_DeveRemoverServico() {
        Servico servico = new Servico();
        servico.setId(3);
        servico.setNome("Troca de Pastilha");
        servico.setValorBase(BigDecimal.valueOf(80.0));

        ItemServico item = new ItemServico();
        item.setId(200);
        item.setServico(servico);
        item.setQuantidade(1);
        item.setValorUnitario(80.0);
        ordemServico.adicionarItemServico(item);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerServico(10, 200);

        assertNotNull(response);
        assertTrue(ordemServico.getItensServico().isEmpty());
    }

    @Test
    @DisplayName("Remover Serviço - Deve lançar RecursoNaoEncontradoException quando item de serviço não existir")
    void removerServico_QuandoItemNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.removerServico(10, 999));
    }
}
