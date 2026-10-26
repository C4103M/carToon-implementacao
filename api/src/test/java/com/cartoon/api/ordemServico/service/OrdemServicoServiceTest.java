package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.AtualizarItemValorRequest;
import com.cartoon.api.ordemServico.dto.request.DescontoRequest;
import com.cartoon.api.ordemServico.models.Desconto;
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
    private Oficina oficinaService;

    @Mock
    private PecaService pecaService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private com.cartoon.api.seguranca.TenantGuard tenantGuard;

    @Spy
    private CalculadoraOrcamento calculadora = new CalculadoraOrcamento();

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    private Veiculo veiculo;
    private Oficina oficina;
    private Usuario mecanico;
    private OrdemServico ordemServico;
    private UsuarioAutenticado solicitante;

    @BeforeEach
    void setUp() {
        solicitante = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");

        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Central");
        oficina.setValorHora(new BigDecimal("100.00"));
        oficina.setTaxaRisco(new BigDecimal("0.10"));
        oficina.setAtivo(true);

        com.cartoon.api.cliente.model.Cliente cliente = new com.cartoon.api.cliente.model.Cliente();
        cliente.setId(1);
        cliente.setNome("Cliente Teste");
        cliente.setAtivo(true);
        cliente.setOficina(oficina);

        veiculo = new Veiculo();
        veiculo.setId(1);
        veiculo.setPlaca("ABC-1234");
        veiculo.setAtivo(true);
        veiculo.setCliente(cliente);

        mecanico = new Usuario();
        mecanico.setId(1);
        mecanico.setNome("Carlos Mecanico");
        mecanico.setRole(Role.MECANICO);
        mecanico.setOficina(oficina);
        mecanico.setAtivo(true);

        ordemServico = new OrdemServico();
        ordemServico.setId(10);
        ordemServico.setDescricao("Troca de Ã³leo");
        ordemServico.setStatusServico(StatusServico.PENDENTE);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setOficina(oficina);
        ordemServico.setMecanico(mecanico);
        ordemServico.setItensPeca(new ArrayList<>());
        ordemServico.setTotal(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Salvar - Deve criar e retornar nova Ordem de ServiÃ§o com data de inicio como agora")
    void salvar_ComDadosValidos_DeveSalvarERetornarResponse() {
        OrdemServicoRequest request = new OrdemServicoRequest(1, 1, "Troca de Ã³leo");

        when(veiculoService.buscarEntidade(1)).thenReturn(veiculo);
        UsuarioAutenticado solicitante = new UsuarioAutenticado(1L, "carlos@cartoon.com", "MECANICO");
        when(usuarioService.buscarEntidade(1)).thenReturn(mecanico);
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(inv -> {
            OrdemServico os = inv.getArgument(0);
            os.setId(10);
            return os;
        });

        OrdemServicoResponse response = ordemServicoService.salvar(request, solicitante);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertEquals("Troca de Ã³leo", response.descricao());
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

        OrdemServicoResponse response = ordemServicoService.buscar(10, solicitante);

        assertNotNull(response);
        assertEquals(10, response.id());
        assertEquals("ABC-1234", response.placa());
        verify(ordemServicoRepository).findById(10);
    }

    @Test
    @DisplayName("Buscar - Deve lanÃ§ar RecursoNaoEncontradoException quando OS nÃ£o for encontrada")
    void buscar_QuandoNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.buscar(99, solicitante));
    }

    @Test
    @DisplayName("Listar - Deve retornar pÃ¡gina de resumos de OrdemServico")
    void listar_DeveRetornarPaginaDeResumos() {
        OrdemServicoFiltro filtro = new OrdemServicoFiltro(1, 1, 1, StatusServico.PENDENTE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(ordemServico), pageable, 1);

        when(ordemServicoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<OrdemServicoResumo> resultado = ordemServicoService.listar(filtro, pageable, solicitante);

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

        OrdemServicoResponse response = ordemServicoService.aceitar(10, solicitante);

        assertEquals(StatusServico.ACEITO, response.status());
        assertEquals(StatusServico.ACEITO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Iniciar - Deve atualizar status para EM_ANDAMENTO")
    void iniciar_DeveAtualizarStatusParaEmAndamento() {
        ordemServico.setStatusServico(StatusServico.ACEITO);
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.iniciar(10, solicitante);

        assertEquals(StatusServico.EM_ANDAMENTO, response.status());
        assertEquals(LocalDate.now(), response.dataInicio());
        assertEquals(StatusServico.EM_ANDAMENTO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Finalizar - Deve atualizar status para FINALIZADO")
    void finalizar_DeveAtualizarStatusParaFinalizado() {
        ordemServico.setStatusServico(StatusServico.EM_ANDAMENTO);
        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setSubtotal(BigDecimal.TEN);
        ordemServico.adicionarItemPeca(itemPeca);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico)).thenReturn(ordemServico);

        OrdemServicoResponse response = ordemServicoService.finalizar(10, solicitante);

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

        OrdemServicoResponse response = ordemServicoService.rejeitar(10, solicitante);

        assertEquals(StatusServico.REJEITADO, response.status());
        assertEquals(LocalDate.now(), response.dataRejeicao());
        assertEquals(StatusServico.REJEITADO, ordemServico.getStatusServico());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Adicionar PeÃ§a - Deve aplicar margemPeca da oficina (ex: 10% em peÃ§a de 100 sai a 110)")
    void adicionarPeca_ComMargemPeca_DeveAplicarMargemSobreValorBase() {
        oficina.setMargemPeca(new BigDecimal("1.10"));
        ItemPecaRequest request = new ItemPecaRequest(5, 1);
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Pastilha de Freio");
        peca.setValorBase(new BigDecimal("100.00"));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(peca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, request, solicitante);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensPeca().size());
        assertEquals(new BigDecimal("110.00"), ordemServico.getItensPeca().get(0).getValorUnitario());
        assertEquals(new BigDecimal("110.00"), ordemServico.getItensPeca().get(0).getSubtotal());
        assertEquals(0, new BigDecimal("110.00").compareTo(response.total()));
    }

    @Test
    @DisplayName("Adicionar Peca - Deve adicionar peca a ordem nao finalizada")
    void adicionarPeca_QuandoOrdemNaoFinalizada_DeveAdicionarPeca() {
        ItemPecaRequest request = new ItemPecaRequest(5, 2);
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Filtro de Ã“leo");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(peca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, request, solicitante);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensPeca().size());
        assertEquals("Filtro de Ã“leo", ordemServico.getItensPeca().get(0).getPeca().getNome());
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Adicionar Peça - Deve lançar ConflitoException quando ordem estiver finalizada")
    void adicionarPeca_QuandoOrdemFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        ItemPecaRequest request = new ItemPecaRequest(5, 2);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarPeca(10, request, solicitante));
    }

    @Test
    @DisplayName("Remover PeÃ§a - Deve remover peÃ§a existente de ordem nÃ£o finalizada")
    void removerPeca_QuandoExiste_DeveRemoverPeca() {
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Filtro de Ã“leo");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        ItemPeca item = new ItemPeca();
        item.setId(100);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorUnitario(BigDecimal.valueOf(50.0));
        ordemServico.adicionarItemPeca(item);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 100, solicitante);

        assertNotNull(response);
        assertTrue(ordemServico.getItensPeca().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.total()));
        assertEquals(0, BigDecimal.ZERO.compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Remover PeÃ§a - Deve atualizar total quando ainda restam itens na ordem")
    void removerPeca_QuandoAindaRestamItens_DeveRecalcularTotal() {
        Peca peca1 = new Peca();
        peca1.setId(1);
        peca1.setNome("Filtro de Ã“leo");
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

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 101, solicitante);

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
        itemServico.setValorUnitario(BigDecimal.valueOf(150.0));
        itemServico.setSubtotal(BigDecimal.valueOf(150.0));
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
    @DisplayName("Adicionar PeÃ§a - Deve acumular valor total quando a ordem jÃ¡ possui outras peÃ§as")
    void adicionarPeca_QuandoJaPossuiPecas_DeveAcumularValorTotal() {
        Peca pecaExistente = new Peca();
        pecaExistente.setId(1);
        pecaExistente.setNome("Ã“leo");
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
        novaPeca.setNome("Vela de IgniÃ§Ã£o");
        novaPeca.setValorBase(BigDecimal.valueOf(30.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(2)).thenReturn(novaPeca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, novaPecaRequest, solicitante);

        assertNotNull(response);
        assertEquals(2, ordemServico.getItensPeca().size());
        // 40.0 + (30.0 * 2) = 100.0
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(100.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Adicionar PeÃ§a - Deve somar peÃ§a nova ao total quando a ordem jÃ¡ possui itens de serviÃ§o")
    void adicionarPeca_QuandoJaPossuiServicos_DeveSomarPecaAosServicos() {
        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(BigDecimal.valueOf(120.0));
        itemServico.setSubtotal(BigDecimal.valueOf(120.0));
        ordemServico.adicionarItemServico(itemServico);
        ordemServico.recalcularTotal();

        ItemPecaRequest request = new ItemPecaRequest(5, 1);
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Correia Dentada");
        peca.setValorBase(BigDecimal.valueOf(80.0));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(pecaService.buscarEntidade(5)).thenReturn(peca);

        OrdemServicoResponse response = ordemServicoService.adicionarPeca(10, request, solicitante);

        assertNotNull(response);
        // 120.0 (serviÃ§o) + 80.0 (peÃ§a) = 200.0
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Remover PeÃ§a - Deve manter valor dos serviÃ§os quando restar apenas itens de serviÃ§o na ordem")
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
        itemServico.setValorUnitario(BigDecimal.valueOf(150.0));
        itemServico.setSubtotal(BigDecimal.valueOf(150.0));
        ordemServico.adicionarItemServico(itemServico);

        ordemServico.recalcularTotal();
        assertEquals(0, BigDecimal.valueOf(200.0).compareTo(ordemServico.getTotal()));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 100, solicitante);

        assertNotNull(response);
        assertTrue(ordemServico.getItensPeca().isEmpty());
        assertEquals(1, ordemServico.getItensServico().size());
        // Deve restar apenas os 150.0 do serviÃ§o
        assertEquals(0, BigDecimal.valueOf(150.0).compareTo(response.total()));
        assertEquals(0, BigDecimal.valueOf(150.0).compareTo(ordemServico.getTotal()));
    }

    @Test
    @DisplayName("Recalcular Total - Deve manter precisÃ£o correta com valores decimais fracionados")
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
        itemServico.setValorUnitario(new BigDecimal("45.30"));
        itemServico.setSubtotal(new BigDecimal("45.30"));
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
        itemServico.setValorUnitario(BigDecimal.valueOf(50.0));
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

        OrdemServicoResponse response = ordemServicoService.buscar(10, solicitante);

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

        Page<OrdemServicoResumo> resultado = ordemServicoService.listar(filtro, pageable, solicitante);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(0, BigDecimal.valueOf(400.0).compareTo(resultado.getContent().get(0).valorTotal()));
    }

    @Test
    @DisplayName("Remover PeÃ§a - Deve lanÃ§ar RecursoNaoEncontradoException quando item de peÃ§a nÃ£o existir")
    void removerPeca_QuandoItemNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.removerPeca(10, 999, solicitante));
    }

    @Test
    @DisplayName("Excluir - Deve excluir ordem quando nÃ£o estiver finalizada")
    void excluir_QuandoNaoFinalizada_DeveExcluir() throws Exception {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        ordemServicoService.excluir(10, solicitante);

        verify(ordemServicoRepository).delete(ordemServico);
    }

    @Test
    @DisplayName("Excluir - Deve lanÃ§ar ConflitoException ao tentar excluir ordem finalizada")
    void excluir_QuandoFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(ConflitoException.class, () -> ordemServicoService.excluir(10, solicitante));
        verify(ordemServicoRepository, never()).delete(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Adicionar ServiÃ§o - Deve adicionar serviÃ§o em ordem nÃ£o finalizada com 1% do valor FIPE")
    void adicionarServico_QuandoOrdemNaoFinalizada_DeveAdicionarServico() {
        veiculo.setValorFipe(new BigDecimal("50000"));
        ItemServicoRequest request = new ItemServicoRequest(3, 1);
        Servico servico = new Servico();
        servico.setId(3);
        servico.setNome("Alinhamento e Balanceamento");
        servico.setQuantidadeHoras(1.5);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(servicoService.buscarEntidade(3)).thenReturn(servico);

        OrdemServicoResponse response = ordemServicoService.adicionarServico(10, request, solicitante);

        assertNotNull(response);
        assertEquals(1, ordemServico.getItensServico().size());
        ItemServico item = ordemServico.getItensServico().get(0);
        assertEquals("Alinhamento e Balanceamento", item.getServico().getNome());
        assertEquals(0, BigDecimal.valueOf(153.75).compareTo(item.getValorUnitario()));
        assertEquals(0, BigDecimal.valueOf(153.75).compareTo(item.getSubtotal()));
        assertEquals(0, new BigDecimal("0.5").compareTo(item.getPesoServico()));
    }

    @Test
    @DisplayName("Adicionar Serviço - Deve lançar ConflitoException quando ordem estiver finalizada")
    void adicionarServico_QuandoOrdemFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        ItemServicoRequest request = new ItemServicoRequest(3, 1);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarServico(10, request, solicitante));
    }

    @Test
    @DisplayName("Remover ServiÃ§o - Deve remover serviÃ§o existente de ordem nÃ£o finalizada")
    void removerServico_QuandoExiste_DeveRemoverServico() {
        Servico servico = new Servico();
        servico.setId(3);
        servico.setNome("Troca de Pastilha");
        servico.setQuantidadeHoras(1.0);

        ItemServico item = new ItemServico();
        item.setId(200);
        item.setServico(servico);
        item.setQuantidade(1);
        item.setValorUnitario(BigDecimal.valueOf(80.0));
        ordemServico.adicionarItemServico(item);

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerServico(10, 200, solicitante);

        assertNotNull(response);
        assertTrue(ordemServico.getItensServico().isEmpty());
    }

    @Test
    @DisplayName("Remover ServiÃ§o - Deve lanÃ§ar RecursoNaoEncontradoException quando item de serviÃ§o nÃ£o existir")
    void removerServico_QuandoItemNaoExiste_DeveLancarExcecao() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        assertThrows(RecursoNaoEncontradoException.class, () -> ordemServicoService.removerServico(10, 999, solicitante));
    }

    @Test
    @DisplayName("Aplicar Desconto - Deve abater percentual da soma total da ordem de serviÃ§o")
    void aplicarDesconto_DeveAbaterPercentualDoTotal() {
        Peca pecaNova = new Peca();
        pecaNova.setId(1);
        pecaNova.setNome("Pastilha");

        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setPeca(pecaNova);
        itemPeca.setQuantidade(1);
        itemPeca.setValorUnitario(new BigDecimal("200.00"));
        itemPeca.calcSubtotal();
        ordemServico.adicionarItemPeca(itemPeca);

        ordemServico.recalcularTotal();
        assertEquals(0, new BigDecimal("200.00").compareTo(ordemServico.getTotal()));

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        DescontoRequest request = new DescontoRequest(Desconto.DEZ_PORCENTO);
        OrdemServicoResponse response = ordemServicoService.aplicarDesconto(10, request, solicitante);

        assertNotNull(response);
        assertEquals(Desconto.DEZ_PORCENTO, response.desconto());
        assertEquals(0, new BigDecimal("200.00").compareTo(response.valorBruto()));
        assertEquals(0, new BigDecimal("20.00").compareTo(response.valorDesconto()));
        assertEquals(0, new BigDecimal("180.00").compareTo(response.total()));
    }

    @Test
    @DisplayName("Aplicar Desconto - Deve lanÃ§ar ConflitoException quando ordem estiver finalizada")
    void aplicarDesconto_QuandoOrdemFinalizada_DeveLancarExcecao() {
        ordemServico.setStatusServico(StatusServico.FINALIZADO);
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        DescontoRequest request = new DescontoRequest(Desconto.QUINZE_PORCENTO);
        assertThrows(ConflitoException.class, () -> ordemServicoService.aplicarDesconto(10, request, solicitante));
    }

    @Test
    @DisplayName("Atualizar Valor Item ServiÃ§o - MecÃ¢nico/Admin pode alterar valor proposto do serviÃ§o e recalcular totais")
    void atualizarValorItemServico_DeveAtualizarValorUnitarioESubtotalETotal() {
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Ã“leo");

        ItemServico item = new ItemServico();
        item.setId(50);
        item.setServico(servico);
        item.setQuantidade(2);
        item.setValorUnitario(new BigDecimal("80.00"));
        item.setSubtotal(new BigDecimal("160.00"));
        ordemServico.adicionarItemServico(item);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.atualizarValorItemServico(10, 50, new BigDecimal("120.00"), solicitante);

        assertNotNull(response);
        assertEquals(new BigDecimal("120.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("240.00"), item.getSubtotal());
        assertEquals(0, new BigDecimal("240.00").compareTo(response.total()));
    }

    @Test
    @DisplayName("Atualizar Peso Item ServiÃ§o - Deve atualizar peso e recalcular valor unitÃ¡rio automaticamente pela fÃ³rmula")
    void atualizarItemServico_ComNovoPeso_DeveRecalcularValorUnitarioPelaFormula() {
        veiculo.setValorFipe(new BigDecimal("50000.00")); // Fipe = 50.000 -> fator = 0.5
        // oficina taxaRisco = 0.10, valorHora = 100.00
        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("ManutenÃ§Ã£o de Freios");
        servico.setQuantidadeHoras(2.0); // 2h -> Base = 200.00

        ItemServico item = new ItemServico();
        item.setId(51);
        item.setServico(servico);
        item.setQuantidadeHoras(2.0);
        item.setQuantidade(1);
        // Inicial com peso 0.5: 200 * (1 + 0.5 * 0.10 * 0.5) = 200 * 1.025 = 205.00
        item.setPesoServico(new BigDecimal("0.5"));
        item.setValorUnitario(new BigDecimal("205.00"));
        item.setSubtotal(new BigDecimal("205.00"));
        ordemServico.adicionarItemServico(item);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        // Atualiza para peso 0.0 (sem risco extra): valor esperado 200.00
        AtualizarItemValorRequest requestSemRisco = new AtualizarItemValorRequest(null, BigDecimal.ZERO);
        OrdemServicoResponse response = ordemServicoService.atualizarItemServico(10, 51, requestSemRisco, solicitante);

        assertNotNull(response);
        assertEquals(new BigDecimal("200.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("200.00"), item.getSubtotal());
        assertEquals(BigDecimal.ZERO, item.getPesoServico());

        // Atualiza para peso 1.0 (alto risco): 200 * (1 + 0.5 * 0.10 * 1.0) = 200 * 1.05 = 210.00
        AtualizarItemValorRequest requestAltoRisco = new AtualizarItemValorRequest(null, BigDecimal.ONE);
        response = ordemServicoService.atualizarItemServico(10, 51, requestAltoRisco, solicitante);

        assertEquals(new BigDecimal("210.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("210.00"), item.getSubtotal());
        assertEquals(BigDecimal.ONE, item.getPesoServico());
    }

    @Test
    @DisplayName("Atualizar Valor Item PeÃ§a - MecÃ¢nico/Admin pode alterar valor da peÃ§a e recalcular subtotal e totais")
    void atualizarValorItemPeca_DeveAtualizarValorUnitarioESubtotalETotal() {
        Peca peca = new Peca();
        peca.setId(5);
        peca.setNome("Pastilha de Freio");
        peca.setValorBase(new BigDecimal("100.00"));

        ItemPeca item = new ItemPeca();
        item.setId(60);
        item.setPeca(peca);
        item.setQuantidade(2);
        item.setValorBase(new BigDecimal("100.00"));
        item.setMargem(new BigDecimal("1.10"));
        item.setValorUnitario(new BigDecimal("110.00"));
        item.setSubtotal(new BigDecimal("220.00"));
        ordemServico.adicionarItemPeca(item);
        ordemServico.recalcularTotal();

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.atualizarValorItemPeca(10, 60, new BigDecimal("130.00"), solicitante);

        assertNotNull(response);
        assertEquals(new BigDecimal("130.00"), item.getValorUnitario());
        assertEquals(new BigDecimal("260.00"), item.getSubtotal());
        assertEquals(new BigDecimal("1.3000"), item.getMargem());
        assertEquals(0, new BigDecimal("260.00").compareTo(response.total()));
    }
}