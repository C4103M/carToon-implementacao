package com.cartoon.api.seguranca;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.oficina.dto.response.OficinaResponse;
import com.cartoon.api.oficina.mapper.OficinaMapper;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.repository.OficinaRepository;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.request.DescontoRequest;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.models.Desconto;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.CalculadoraOrcamento;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.repository.VeiculoRepository;
import com.cartoon.api.veiculo.service.VeiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantIsolationTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private OficinaRepository oficinaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private PecaService pecaService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private CalculadoraOrcamento calculadoraOrcamento;

    @Mock
    private OficinaMapper oficinaMapper;

    private TenantGuard tenantGuard;
    private OrdemServicoService ordemServicoService;
    private VeiculoService veiculoService;
    private OficinaService oficinaService;

    private Oficina oficinaA;
    private Oficina oficinaB;
    private Usuario usuarioOficinaA;
    private UsuarioAutenticado solicitanteOficinaA;

    @BeforeEach
    void setUp() {
        tenantGuard = new TenantGuard(usuarioRepository);

        oficinaA = new Oficina();
        oficinaA.setId(1);
        oficinaA.setNome("Oficina A");
        oficinaA.setAtivo(true);

        oficinaB = new Oficina();
        oficinaB.setId(2);
        oficinaB.setNome("Oficina B");
        oficinaB.setAtivo(true);

        usuarioOficinaA = new Usuario();
        usuarioOficinaA.setId(10);
        usuarioOficinaA.setNome("Mecânico A");
        usuarioOficinaA.setRole(Role.MECANICO);
        usuarioOficinaA.setOficina(oficinaA);

        solicitanteOficinaA = new UsuarioAutenticado(10L, "mecanico@oficinaa.com", "ROLE_MECANICO");

        ordemServicoService = new OrdemServicoService(
                ordemServicoRepository,
                null,
                usuarioService,
                pecaService,
                servicoService,
                calculadoraOrcamento,
                tenantGuard
        );

        veiculoService = new VeiculoService(
                veiculoRepository,
                clienteService,
                ordemServicoService,
                null,
                tenantGuard
        );

        oficinaService = new OficinaService(
                oficinaRepository,
                ordemServicoRepository,
                oficinaMapper,
                usuarioService
        );
    }

    private OrdemServico criarOrdemServicoOficinaB() {
        OrdemServico os = new OrdemServico();
        os.setId(100);
        os.setOficina(oficinaB);
        return os;
    }

    private Veiculo criarVeiculoOficinaB() {
        Cliente clienteB = new Cliente();
        clienteB.setId(20);
        clienteB.setNome("Cliente B");
        clienteB.setOficina(oficinaB);
        clienteB.setAtivo(true);

        Veiculo veiculoB = new Veiculo();
        veiculoB.setId(200);
        veiculoB.setPlaca("BRA2E19");
        veiculoB.setCliente(clienteB);
        veiculoB.setAtivo(true);
        return veiculoB;
    }

    // ==========================================
    // S1. Testes de IDOR em Ordem de Serviço
    // ==========================================

    @Test
    @DisplayName("S1: Usuário da Oficina A não pode ler OS da Oficina B (403 Forbidden)")
    void buscarOS_outraOficina_deveLancarAccessDeniedException() {
        OrdemServico osB = criarOrdemServicoOficinaB();
        when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(osB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> ordemServicoService.buscar(100, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para acessar ou alterar ordens de serviço de outra oficina.");
    }

    @Test
    @DisplayName("S1: Usuário da Oficina A não pode aceitar OS da Oficina B")
    void aceitarOS_outraOficina_deveLancarAccessDeniedException() {
        OrdemServico osB = criarOrdemServicoOficinaB();
        when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(osB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> ordemServicoService.aceitar(100, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("S1: Usuário da Oficina A não pode rejeitar OS da Oficina B")
    void rejeitarOS_outraOficina_deveLancarAccessDeniedException() {
        OrdemServico osB = criarOrdemServicoOficinaB();
        when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(osB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> ordemServicoService.rejeitar(100, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("S1: Usuário da Oficina A não pode adicionar peça a OS da Oficina B")
    void adicionarPecaOS_outraOficina_deveLancarAccessDeniedException() {
        OrdemServico osB = criarOrdemServicoOficinaB();
        when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(osB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        ItemPecaRequest req = new ItemPecaRequest(1, 2);

        assertThatThrownBy(() -> ordemServicoService.adicionarPeca(100, req, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("S1: Usuário da Oficina A não pode aplicar desconto em OS da Oficina B")
    void aplicarDescontoOS_outraOficina_deveLancarAccessDeniedException() {
        OrdemServico osB = criarOrdemServicoOficinaB();
        when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(osB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        DescontoRequest req = new DescontoRequest(Desconto.CINCO_PORCENTO);

        assertThatThrownBy(() -> ordemServicoService.aplicarDesconto(100, req, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==========================================
    // S2. Testes de Filtro de Oficina na Listagem de OS
    // ==========================================

    @Test
    @DisplayName("S2: Não-superadmin tentando passar oficinaId=2 deve ter filtro sobrescrito com sua oficinaId=1")
    void listarOS_comOficinaIdMaliciosa_deveSobrescreverComOficinaDoUsuario() {
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));
        when(ordemServicoRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Atacante passa oficinaId=2
        OrdemServicoFiltro filtroAtacante = new OrdemServicoFiltro(2, null, null, null, null, null);

        ordemServicoService.listar(filtroAtacante, PageRequest.of(0, 10), solicitanteOficinaA);

        // Verifica que o repository foi consultado com a especificação gerada
        verify(ordemServicoRepository).findAll(any(Specification.class), eq(PageRequest.of(0, 10)));
    }

    // ==========================================
    // S3. Testes de Isolamento do Módulo Veículo
    // ==========================================

    @Test
    @DisplayName("S3: Usuário da Oficina A não pode buscar veículo da Oficina B por ID")
    void buscarVeiculoPorId_outraOficina_deveLancarAccessDeniedException() {
        Veiculo veiculoB = criarVeiculoOficinaB();
        when(veiculoRepository.findComClienteById(200)).thenReturn(Optional.of(veiculoB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> veiculoService.buscarPorId(200, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não tem permissão para acessar ou alterar veículos de outra oficina.");
    }

    @Test
    @DisplayName("S3: Usuário da Oficina A não pode buscar veículo da Oficina B por placa")
    void buscarVeiculoPorPlaca_outraOficina_deveLancarAccessDeniedException() {
        Veiculo veiculoB = criarVeiculoOficinaB();
        when(veiculoRepository.findComClienteByPlacaIgnoreCase("BRA2E19")).thenReturn(Optional.of(veiculoB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> veiculoService.buscarPorPlaca("BRA2E19", solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("S3: Usuário da Oficina A não pode inativar veículo da Oficina B")
    void inativarVeiculo_outraOficina_deveLancarAccessDeniedException() {
        Veiculo veiculoB = criarVeiculoOficinaB();
        when(veiculoRepository.findById(200)).thenReturn(Optional.of(veiculoB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> veiculoService.inativar(200, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("S3: Usuário da Oficina A não pode cadastrar veículo para cliente da Oficina B")
    void cadastrarVeiculo_clienteOutraOficina_deveLancarAccessDeniedException() {
        Cliente clienteB = new Cliente();
        clienteB.setId(20);
        clienteB.setOficina(oficinaB);
        clienteB.setAtivo(true);

        when(clienteService.buscarEntidade(20)).thenReturn(clienteB);
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        VeiculoRequest req = new VeiculoRequest(20, "BRA2E19", "Fiat", "Uno", 2020, "Flex", null, null, null);

        assertThatThrownBy(() -> veiculoService.cadastrar(req, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você não pode cadastrar veículos para clientes de outra oficina.");
    }

    @Test
    @DisplayName("S3: Usuário da Oficina A não pode consultar histórico de manutenção de veículo da Oficina B")
    void consultarHistorico_veiculoOutraOficina_deveLancarAccessDeniedException() {
        Veiculo veiculoB = criarVeiculoOficinaB();
        when(veiculoRepository.findComClienteByPlacaIgnoreCase("BRA2E19")).thenReturn(Optional.of(veiculoB));
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioOficinaA));

        assertThatThrownBy(() -> veiculoService.consultarHistorico("BRA2E19", solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==========================================
    // S7. Testes de Isolamento de Dados em /oficina
    // ==========================================

    @Test
    @DisplayName("S7: Usuário da Oficina A não pode visualizar dados financeiros de outra oficina por ID")
    void buscarOficinaPorId_outraOficina_deveLancarAccessDeniedException() {
        when(oficinaRepository.findById(2)).thenReturn(Optional.of(oficinaB));
        when(usuarioService.buscarEntidade(10)).thenReturn(usuarioOficinaA);

        assertThatThrownBy(() -> oficinaService.buscarPorId(2, solicitanteOficinaA))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Você só pode visualizar os dados da oficina à qual pertence.");
    }

    @Test
    @DisplayName("S7: Usuário da Oficina A listando oficinas recebe apenas a sua própria")
    void listarOficinas_usuarioNaoSuperAdmin_recebeApenasSuaOficina() {
        when(usuarioService.buscarEntidade(10)).thenReturn(usuarioOficinaA);
        OficinaResponse responseA = new OficinaResponse(1, "Oficina A", "Endereço A", "11999999999", "contato@a.com",
                new BigDecimal("100.00"), new BigDecimal("0.10"), new BigDecimal("1.15"), true);
        when(oficinaMapper.toResponse(oficinaA)).thenReturn(responseA);

        List<OficinaResponse> resultado = oficinaService.listar(null, solicitanteOficinaA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1);
        verify(oficinaRepository, never()).findAll();
    }
}
