package com.cartoon.api.ordemServico.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.dto.request.AtualizarItemValorRequest;
import com.cartoon.api.ordemServico.dto.request.DescontoRequest;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.models.*;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.seguranca.TenantGuard;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.service.VeiculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoRegrasNegocioTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private PecaService pecaService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private TenantGuard tenantGuard;

    @Spy
    private CalculadoraOrcamento calculadora = new CalculadoraOrcamento();

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    private Oficina oficina1;
    private Oficina oficina2;
    private Cliente cliente1;
    private Cliente cliente2;
    private Veiculo veiculo1;
    private Usuario mecanico1;
    private Usuario mecanico2;
    private OrdemServico os;

    private UsuarioAutenticado adminAuth;
    private UsuarioAutenticado mecanicoAuth;

    @BeforeEach
    void setUp() {
        adminAuth = new UsuarioAutenticado(10L, "admin@teste.com", "ROLE_ADMIN");
        mecanicoAuth = new UsuarioAutenticado(20L, "mecanico@teste.com", "ROLE_MECANICO");

        oficina1 = new Oficina();
        oficina1.setId(1);
        oficina1.setNome("Oficina 1");
        oficina1.setValorHora(new BigDecimal("100.00"));
        oficina1.setTaxaRisco(new BigDecimal("0.10"));
        oficina1.setAtivo(true);

        oficina2 = new Oficina();
        oficina2.setId(2);
        oficina2.setNome("Oficina 2");
        oficina2.setAtivo(true);

        lenient().when(tenantGuard.obterOficinaDoSolicitante(any())).thenReturn(oficina1);

        cliente1 = new Cliente();
        cliente1.setId(1);
        cliente1.setNome("Cliente 1");
        cliente1.setAtivo(true);
        cliente1.setOficina(oficina1);

        cliente2 = new Cliente();
        cliente2.setId(2);
        cliente2.setNome("Cliente 2");
        cliente2.setAtivo(true);
        cliente2.setOficina(oficina2);

        veiculo1 = new Veiculo();
        veiculo1.setId(1);
        veiculo1.setPlaca("ABC1234");
        veiculo1.setAtivo(true);
        veiculo1.setCliente(cliente1);
        veiculo1.setValorFipe(new BigDecimal("50000.00"));

        mecanico1 = new Usuario();
        mecanico1.setId(20);
        mecanico1.setNome("Mecanico 1");
        mecanico1.setAtivo(true);
        mecanico1.setRole(Role.MECANICO);
        mecanico1.setOficina(oficina1);

        mecanico2 = new Usuario();
        mecanico2.setId(30);
        mecanico2.setNome("Mecanico 2");
        mecanico2.setAtivo(true);
        mecanico2.setRole(Role.MECANICO);
        mecanico2.setOficina(oficina2);

        os = new OrdemServico();
        os.setId(100);
        os.setOficina(oficina1);
        os.setVeiculo(veiculo1);
        os.setMecanico(mecanico1);
        os.setStatusServico(StatusServico.PENDENTE);
        os.setItensPeca(new ArrayList<>());
        os.setItensServico(new ArrayList<>());
        os.setTotal(BigDecimal.ZERO);
    }

    @Nested
    @DisplayName("N2 - Máquina de Estados da OS")
    class MaquinaDeEstadosTests {

        @Test
        @DisplayName("Aceitar - deve permitir apenas a partir de PENDENTE")
        void aceitarApenasDePendente() {
            os.setStatusServico(StatusServico.PENDENTE);
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));
            when(ordemServicoRepository.save(any())).thenReturn(os);

            assertDoesNotThrow(() -> ordemServicoService.aceitar(100, adminAuth));
            assertEquals(StatusServico.ACEITO, os.getStatusServico());

            for (StatusServico status : List.of(StatusServico.ACEITO, StatusServico.EM_ANDAMENTO, StatusServico.FINALIZADO, StatusServico.REJEITADO)) {
                os.setStatusServico(status);
                assertThrows(ConflitoException.class, () -> ordemServicoService.aceitar(100, adminAuth));
            }
        }

        @Test
        @DisplayName("Iniciar - deve permitir apenas a partir de ACEITO")
        void iniciarApenasDeAceito() {
            os.setStatusServico(StatusServico.ACEITO);
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));
            when(ordemServicoRepository.save(any())).thenReturn(os);

            assertDoesNotThrow(() -> ordemServicoService.iniciar(100, adminAuth));
            assertEquals(StatusServico.EM_ANDAMENTO, os.getStatusServico());
            assertNotNull(os.getDataInicio());

            for (StatusServico status : List.of(StatusServico.PENDENTE, StatusServico.EM_ANDAMENTO, StatusServico.FINALIZADO, StatusServico.REJEITADO)) {
                os.setStatusServico(status);
                assertThrows(ConflitoException.class, () -> ordemServicoService.iniciar(100, adminAuth));
            }
        }

        @Test
        @DisplayName("Finalizar - deve permitir apenas a partir de EM_ANDAMENTO e com itens")
        void finalizarApenasDeEmAndamentoEComItens() {
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));

            // EM_ANDAMENTO mas sem itens -> lança RegraDeNegocioException
            os.setStatusServico(StatusServico.EM_ANDAMENTO);
            os.getItensPeca().clear();
            os.getItensServico().clear();
            assertThrows(RegraDeNegocioException.class, () -> ordemServicoService.finalizar(100, adminAuth));

            // Adiciona item de peça
            ItemPeca itemPeca = new ItemPeca();
            itemPeca.setId(1);
            os.getItensPeca().add(itemPeca);
            when(ordemServicoRepository.save(any())).thenReturn(os);

            assertDoesNotThrow(() -> ordemServicoService.finalizar(100, adminAuth));
            assertEquals(StatusServico.FINALIZADO, os.getStatusServico());
            assertNotNull(os.getDataFinalizacao());

            // A partir de outros status -> ConflitoException
            for (StatusServico status : List.of(StatusServico.PENDENTE, StatusServico.ACEITO, StatusServico.FINALIZADO, StatusServico.REJEITADO)) {
                os.setStatusServico(status);
                assertThrows(ConflitoException.class, () -> ordemServicoService.finalizar(100, adminAuth));
            }
        }

        @Test
        @DisplayName("Rejeitar - deve permitir apenas a partir de PENDENTE ou ACEITO")
        void rejeitarApenasDePendenteOuAceito() {
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));
            when(ordemServicoRepository.save(any())).thenReturn(os);

            // De PENDENTE
            os.setStatusServico(StatusServico.PENDENTE);
            assertDoesNotThrow(() -> ordemServicoService.rejeitar(100, adminAuth));
            assertEquals(StatusServico.REJEITADO, os.getStatusServico());
            assertNotNull(os.getDataRejeicao());

            // De ACEITO
            os.setStatusServico(StatusServico.ACEITO);
            assertDoesNotThrow(() -> ordemServicoService.rejeitar(100, adminAuth));
            assertEquals(StatusServico.REJEITADO, os.getStatusServico());

            // De EM_ANDAMENTO ou FINALIZADO
            os.setStatusServico(StatusServico.EM_ANDAMENTO);
            assertThrows(ConflitoException.class, () -> ordemServicoService.rejeitar(100, adminAuth));

            os.setStatusServico(StatusServico.FINALIZADO);
            assertThrows(ConflitoException.class, () -> ordemServicoService.rejeitar(100, adminAuth));
        }
    }

    @Nested
    @DisplayName("N6 - Imutabilidade pós-orçamento (Apenas PENDENTE pode ser alterado)")
    class ImutabilidadeItensTests {

        @Test
        @DisplayName("Não deve permitir modificar itens ou desconto se OS não for PENDENTE")
        void bloquearModificacaoItensForaDePendente() {
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));

            for (StatusServico status : List.of(StatusServico.ACEITO, StatusServico.EM_ANDAMENTO, StatusServico.FINALIZADO, StatusServico.REJEITADO)) {
                os.setStatusServico(status);

                assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarPeca(100, new ItemPecaRequest(1, 2), adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.removerPeca(100, 1, adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.adicionarServico(100, new ItemServicoRequest(1, 1, new BigDecimal("0.5")), adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.removerServico(100, 1, adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.atualizarValorItemPeca(100, 1, new BigDecimal("50.00"), adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.atualizarItemServico(100, 1, new AtualizarItemValorRequest(null, null), adminAuth));
                assertThrows(ConflitoException.class, () -> ordemServicoService.aplicarDesconto(100, new DescontoRequest(Desconto.DEZ_PORCENTO), adminAuth));
            }
        }

        @Test
        @DisplayName("Excluir - não deve permitir excluir OS EM_ANDAMENTO ou FINALIZADO")
        void bloquearExclusaoEmAndamentoOuFinalizado() {
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));

            os.setStatusServico(StatusServico.EM_ANDAMENTO);
            assertThrows(ConflitoException.class, () -> ordemServicoService.excluir(100, adminAuth));

            os.setStatusServico(StatusServico.FINALIZADO);
            assertThrows(ConflitoException.class, () -> ordemServicoService.excluir(100, adminAuth));
        }
    }

    @Nested
    @DisplayName("N7 - Validação de vínculos ao salvar OS")
    class ValidacaoVinculosSalvarTests {

        @Test
        @DisplayName("Deve rejeitar salvar OS se a oficina estiver inativa")
        void rejeitarOficinaInativa() {
            Oficina oficinaInativa = new Oficina();
            oficinaInativa.setId(1);
            oficinaInativa.setAtivo(false);
            when(tenantGuard.obterOficinaDoSolicitante(any())).thenReturn(oficinaInativa);
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 20, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("inativa"));
        }

        @Test
        @DisplayName("Deve rejeitar salvar OS se o mecânico estiver inativo")
        void rejeitarMecanicoInativo() {
            mecanico1.setAtivo(false);
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);
            when(usuarioService.buscarEntidade(20)).thenReturn(mecanico1);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 20, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("inativo"));
        }

        @Test
        @DisplayName("Deve rejeitar salvar OS se o mecânico pertencer a outra oficina")
        void rejeitarMecanicoOutraOficina() {
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);
            when(usuarioService.buscarEntidade(30)).thenReturn(mecanico2);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 30, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("pertence a outra oficina"));
        }

        @Test
        @DisplayName("Deve rejeitar salvar OS se o veículo estiver inativo")
        void rejeitarVeiculoInativo() {
            veiculo1.setAtivo(false);
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);
            when(usuarioService.buscarEntidade(20)).thenReturn(mecanico1);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 20, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("inativo"));
        }

        @Test
        @DisplayName("Deve rejeitar salvar OS se o cliente estiver inativo")
        void rejeitarClienteInativo() {
            cliente1.setAtivo(false);
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);
            when(usuarioService.buscarEntidade(20)).thenReturn(mecanico1);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 20, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("inativo"));
        }

        @Test
        @DisplayName("Deve rejeitar salvar OS se o cliente pertencer a outra oficina")
        void rejeitarClienteOutraOficina() {
            veiculo1.setCliente(cliente2); // cliente da oficina 2
            when(veiculoService.buscarEntidade(1)).thenReturn(veiculo1);
            when(usuarioService.buscarEntidade(20)).thenReturn(mecanico1);

            OrdemServicoRequest request = new OrdemServicoRequest(1, 20, "Revisao");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.salvar(request, adminAuth));
            assertTrue(ex.getMessage().contains("pertence a um cliente de outra oficina"), () -> "Mensagem recebida foi: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("N5 / N13 - Proteção de margens e preços")
    class ProtecaoPrecosEMargensTests {

        @Test
        @DisplayName("Mecânico não pode vender peça abaixo do valorBase")
        void mecanicoNaoPodeVenderAbaixoValorBase() {
            Peca peca = new Peca();
            peca.setId(1);
            peca.setNome("Pastilha");
            peca.setValorBase(new BigDecimal("100.00"));

            ItemPeca itemPeca = new ItemPeca();
            itemPeca.setId(5);
            itemPeca.setPeca(peca);
            itemPeca.setValorUnitario(new BigDecimal("120.00"));
            itemPeca.setQuantidade(1);
            itemPeca.setSubtotal(new BigDecimal("120.00"));

            os.getItensPeca().add(itemPeca);
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));

            // Tentativa de mecânico com preço R$ 99.00 (< 100.00) -> lança RegraDeNegocioException
            BigDecimal precoAbaixo = new BigDecimal("99.00");
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
                    () -> ordemServicoService.atualizarValorItemPeca(100, 5, precoAbaixo, mecanicoAuth));
            assertTrue(ex.getMessage().contains("abaixo do custo base"));

            // Tentativa de mecânico com preço R$ 100.00 (igual) ou maior -> sucesso
            when(ordemServicoRepository.save(any())).thenReturn(os);
            BigDecimal precoValido = new BigDecimal("105.00");
            assertDoesNotThrow(() -> ordemServicoService.atualizarValorItemPeca(100, 5, precoValido, mecanicoAuth));
        }

        @Test
        @DisplayName("Mecânico não pode alterar diretamente o valorUnitario de um serviço")
        void mecanicoNaoPodeAlterarValorUnitarioServicoDireto() {
            Servico servico = new Servico();
            servico.setId(1);
            servico.setNome("Troca de Pastilha");

            ItemServico itemServico = new ItemServico();
            itemServico.setId(7);
            itemServico.setServico(servico);
            itemServico.setValorUnitario(new BigDecimal("150.00"));
            itemServico.setPesoServico(new BigDecimal("0.50"));

            os.getItensServico().add(itemServico);
            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));

            AtualizarItemValorRequest requestPreco = new AtualizarItemValorRequest(new BigDecimal("80.00"), null);

            assertThrows(AccessDeniedException.class,
                    () -> ordemServicoService.atualizarItemServico(100, 7, requestPreco, mecanicoAuth));

            // Admin pode alterar
            when(ordemServicoRepository.save(any())).thenReturn(os);
            assertDoesNotThrow(() -> ordemServicoService.atualizarItemServico(100, 7, requestPreco, adminAuth));
        }
    }
}
