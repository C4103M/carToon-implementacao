package com.cartoon.api.seguranca;

import com.cartoon.api.auth.AuthService;
import com.cartoon.api.auth.LoginAttemptService;
import com.cartoon.api.auth.LoginDTO;
import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.compartilhado.exceptions.RegraDeNegocioException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.repository.OficinaRepository;
import com.cartoon.api.oficina.service.OficinaService;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.ItemServicoRequest;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.CalculadoraOrcamento;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.repositories.PecaRepository;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repository.ServicoRepository;
import com.cartoon.api.servico.service.ServicoService;
import com.cartoon.api.usuario.dto.request.AtualizarUsuarioRequest;
import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import com.cartoon.api.usuario.service.UsuarioService;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.model.Veiculo;
import com.cartoon.api.veiculo.repository.VeiculoRepository;
import com.cartoon.api.veiculo.service.VeiculoService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HardeningIteracao4Test {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nested
    @DisplayName("S6 & S9 - Segurança em Autenticação e JWT Filter")
    class SegurancaAuthTests {

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private JwtService jwtService;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private LoginAttemptService loginAttemptService;

        @Mock
        private HttpServletRequest request;

        @Mock
        private HttpServletResponse response;

        @Mock
        private FilterChain filterChain;

        @Mock
        private Claims claims;

        @BeforeEach
        void clearSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("JwtCookieFilter - Rejeita autenticação quando usuário estiver inativo no banco")
        void jwtFilter_RejeitaUsuarioInativo() throws Exception {
            JwtCookieFilter filter = new JwtCookieFilter(jwtService, usuarioRepository);

            when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
            when(jwtService.validar("token_valido")).thenReturn(claims);
            when(claims.get("id", Number.class)).thenReturn(10);

            Usuario usuarioInativo = new Usuario();
            usuarioInativo.setId(10);
            usuarioInativo.setAtivo(false);
            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuarioInativo));

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("JwtCookieFilter - Rejeita autenticação quando oficina vinculada estiver inativa")
        void jwtFilter_RejeitaOficinaInativa() throws Exception {
            JwtCookieFilter filter = new JwtCookieFilter(jwtService, usuarioRepository);

            when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
            when(jwtService.validar("token_valido")).thenReturn(claims);
            when(claims.get("id", Number.class)).thenReturn(10);

            Oficina oficinaInativa = new Oficina();
            oficinaInativa.setId(1);
            oficinaInativa.setAtivo(false);

            Usuario usuario = new Usuario();
            usuario.setId(10);
            usuario.setAtivo(true);
            usuario.setRole(Role.MECANICO);
            usuario.setOficina(oficinaInativa);

            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("JwtCookieFilter - Autentica com sucesso usuário ativo e reflete role atualizada do banco")
        void jwtFilter_AutenticaUsuarioAtivoComRoleAtualizada() throws Exception {
            JwtCookieFilter filter = new JwtCookieFilter(jwtService, usuarioRepository);

            when(request.getHeader("Authorization")).thenReturn("Bearer token_valido");
            when(jwtService.validar("token_valido")).thenReturn(claims);
            when(claims.get("id", Number.class)).thenReturn(10);

            Oficina oficinaAtiva = new Oficina();
            oficinaAtiva.setId(1);
            oficinaAtiva.setAtivo(true);

            Usuario usuario = new Usuario();
            usuario.setId(10);
            usuario.setNome("Joao");
            usuario.setEmail("joao@teste.com");
            usuario.setAtivo(true);
            usuario.setRole(Role.ADMIN);
            usuario.setOficina(oficinaAtiva);

            when(usuarioRepository.findById(10)).thenReturn(Optional.of(usuario));

            filter.doFilter(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals("ROLE_ADMIN", auth.getAuthorities().iterator().next().getAuthority());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("AuthService - Bloqueia login se oficina vinculada estiver inativa")
        void authService_BloqueiaLoginOficinaInativa() {
            AuthService authService = new AuthService(usuarioRepository, jwtService, passwordEncoder, loginAttemptService);

            Oficina oficinaInativa = new Oficina();
            oficinaInativa.setId(1);
            oficinaInativa.setAtivo(false);

            Usuario usuario = new Usuario();
            usuario.setId(1);
            usuario.setEmail("admin@oficina.com");
            usuario.setAtivo(true);
            usuario.setRole(Role.ADMIN);
            usuario.setOficina(oficinaInativa);

            when(usuarioRepository.findByEmailIgnoreCase("admin@oficina.com")).thenReturn(Optional.of(usuario));

            assertThrows(CredenciaisInvalidasException.class,
                    () -> authService.login(new LoginDTO("admin@oficina.com", "senha123")));
            verify(loginAttemptService).registrarFalha("admin@oficina.com");
        }

        @Test
        @DisplayName("S9 - LoginAttemptService - Expurgo por limite de capacidade (mitigação DoS)")
        void loginAttemptService_ExpurgoCapacidade() {
            LoginAttemptService service = new LoginAttemptService();
            for (int i = 0; i < LoginAttemptService.MAX_ENTRIES + 100; i++) {
                service.registrarFalha("atacante_" + i + "@teste.com");
            }
            assertTrue(service.getTentativasFalhas("atacante_0@teste.com") <= 1);
        }
    }

    @Nested
    @DisplayName("S8 - Confirmação de Senha Atual ao Alterar Credenciais")
    class SenhaAtualAlteracaoCredenciaisTests {

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private OrdemServicoRepository ordemServicoRepository;

        @InjectMocks
        private UsuarioService usuarioService;

        private Usuario usuario;
        private UsuarioAutenticado solicitante;

        @BeforeEach
        void setUp() {
            usuario = new Usuario();
            usuario.setId(5);
            usuario.setNome("Carlos Mecanico");
            usuario.setEmail("carlos@oficina.com");
            usuario.setSenha("$2a$10$hashAtual");
            usuario.setRole(Role.MECANICO);
            usuario.setAtivo(true);

            solicitante = new UsuarioAutenticado(5L, "carlos@oficina.com", "ROLE_MECANICO");
            lenient().when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario));
        }

        @Test
        @DisplayName("Exige senha atual ao alterar a senha do próprio usuário")
        void alterarSenha_SemSenhaAtual_LancaExcecao() {
            AtualizarUsuarioRequest req = new AtualizarUsuarioRequest(
                    "Carlos Mecanico", "carlos@oficina.com", null, "novaSenha123", "novaSenha123", null
            );

            assertThrows(RegraDeNegocioException.class, () -> usuarioService.atualizarMeusDados(req, solicitante));
        }

        @Test
        @DisplayName("Exige senha atual correta ao alterar a senha do próprio usuário")
        void alterarSenha_SenhaAtualIncorreta_LancaExcecao() {
            when(passwordEncoder.matches("senhaErrada", "$2a$10$hashAtual")).thenReturn(false);

            AtualizarUsuarioRequest req = new AtualizarUsuarioRequest(
                    "Carlos Mecanico", "carlos@oficina.com", null, "novaSenha123", "novaSenha123", "senhaErrada"
            );

            assertThrows(RegraDeNegocioException.class, () -> usuarioService.atualizarMeusDados(req, solicitante));
        }

        @Test
        @DisplayName("Exige senha atual ao alterar o próprio e-mail")
        void alterarEmail_SemSenhaAtual_LancaExcecao() {
            AtualizarUsuarioRequest req = new AtualizarUsuarioRequest(
                    "Carlos Mecanico", "novo.email@oficina.com", null, null, null, null
            );

            assertThrows(RegraDeNegocioException.class, () -> usuarioService.atualizarMeusDados(req, solicitante));
        }

        @Test
        @DisplayName("Permite alterar e-mail e senha quando senha atual estiver correta")
        void alterarEmailESenha_ComSenhaAtualCorreta_Sucesso() {
            when(passwordEncoder.matches("senhaCorreta", "$2a$10$hashAtual")).thenReturn(true);
            when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$novaHash");

            AtualizarUsuarioRequest req = new AtualizarUsuarioRequest(
                    "Carlos Mecanico", "novo.email@oficina.com", null, "novaSenha123", "novaSenha123", "senhaCorreta"
            );

            var res = usuarioService.atualizarMeusDados(req, solicitante);
            assertNotNull(res);
            assertEquals("novo.email@oficina.com", res.email());
        }

        @Test
        @DisplayName("Permite alterar nome sem senha atual se e-mail e senha permanecerem inalterados")
        void alterarApenasNome_NaoExigeSenhaAtual() {
            AtualizarUsuarioRequest req = new AtualizarUsuarioRequest(
                    "Novo Nome", "carlos@oficina.com", null, null, null, null
            );

            var res = usuarioService.atualizarMeusDados(req, solicitante);
            assertNotNull(res);
            assertEquals("Novo Nome", res.nome());
        }
    }

    @Nested
    @DisplayName("N8 - Integridade e Cascata de Inativações")
    class IntegridadeInativacoesTests {

        @Mock
        private ClienteRepository clienteRepository;

        @Mock
        private VeiculoRepository veiculoRepository;

        @Mock
        private OrdemServicoRepository ordemServicoRepository;

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private UsuarioService usuarioService;

        @InjectMocks
        private ClienteService clienteService;

        @InjectMocks
        private OficinaService oficinaService;

        private final UsuarioAutenticado superAdmin = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");

        @Test
        @DisplayName("ClienteService - Bloqueia inativar cliente com OS aberta")
        void clienteService_BloqueiaInativarComOsAberta() {
            Cliente cliente = new Cliente();
            cliente.setId(10);
            cliente.setAtivo(true);
            when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
            when(ordemServicoRepository.existsByVeiculoClienteIdAndStatusServicoIn(eq(10), anyCollection())).thenReturn(true);

            assertThrows(ConflitoException.class, () -> clienteService.inativar(10, superAdmin));
            assertTrue(cliente.isAtivo());
        }

        @Test
        @DisplayName("ClienteService - Inativação do cliente inativa em cascata seus veículos")
        void clienteService_InativaVeiculosEmCascata() {
            Cliente cliente = new Cliente();
            cliente.setId(10);
            cliente.setAtivo(true);
            Veiculo v1 = new Veiculo();
            v1.setAtivo(true);
            Veiculo v2 = new Veiculo();
            v2.setAtivo(true);
            cliente.setVeiculos(List.of(v1, v2));

            when(clienteRepository.findById(10)).thenReturn(Optional.of(cliente));
            when(ordemServicoRepository.existsByVeiculoClienteIdAndStatusServicoIn(eq(10), anyCollection())).thenReturn(false);

            clienteService.inativar(10, superAdmin);

            assertFalse(cliente.isAtivo());
            assertFalse(v1.getAtivo());
            assertFalse(v2.getAtivo());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("OficinaService - Inativação da oficina propaga inativação para os usuários")
        void oficinaService_DesativarInativaUsuarios() {
            Oficina oficina = new Oficina();
            oficina.setId(20);
            oficina.setAtivo(true);

            Usuario u1 = new Usuario();
            u1.setAtivo(true);
            Usuario u2 = new Usuario();
            u2.setAtivo(true);

            OficinaRepository oficinaRepoMock = mock(OficinaRepository.class);
            when(oficinaRepoMock.findById(20)).thenReturn(Optional.of(oficina));
            when(ordemServicoRepository.existsByOficinaIdAndStatusServicoIn(eq(20), anyCollection())).thenReturn(false);
            when(usuarioRepository.findAllByOficinaId(20)).thenReturn(List.of(u1, u2));

            OficinaService service = new OficinaService(
                    oficinaRepoMock, ordemServicoRepository, null, usuarioService, usuarioRepository
            );

            service.desativar(20);

            assertFalse(oficina.getAtivo());
            assertFalse(u1.getAtivo());
            assertFalse(u2.getAtivo());
        }
    }

    @Nested
    @DisplayName("N9 - Soft Delete de Peças e Serviços e Bloqueio na OS")
    class SoftDeletePecasServicosTests {

        @Mock
        private OrdemServicoRepository ordemServicoRepository;

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

        private final UsuarioAutenticado adminAuth = new UsuarioAutenticado(1L, "admin@teste.com", "ROLE_ADMIN");

        @Test
        @DisplayName("Bloqueia adicionar peça inativa na OS")
        void os_BloqueiaPecaInativa() {
            OrdemServico os = new OrdemServico();
            os.setId(100);
            os.setStatusServico(StatusServico.PENDENTE);

            Peca pecaInativa = new Peca();
            pecaInativa.setId(1);
            pecaInativa.setAtivo(false);

            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));
            when(pecaService.buscarEntidade(1)).thenReturn(pecaInativa);

            ItemPecaRequest req = new ItemPecaRequest(1, 2);
            assertThrows(RegraDeNegocioException.class, () -> ordemServicoService.adicionarPeca(100, req, adminAuth));
        }

        @Test
        @DisplayName("Bloqueia adicionar serviço inativo na OS")
        void os_BloqueiaServicoInativo() {
            OrdemServico os = new OrdemServico();
            os.setId(100);
            os.setStatusServico(StatusServico.PENDENTE);

            Servico servicoInativo = new Servico();
            servicoInativo.setId(2);
            servicoInativo.setAtivo(false);

            when(ordemServicoRepository.findById(100)).thenReturn(Optional.of(os));
            when(servicoService.buscarEntidade(2)).thenReturn(servicoInativo);

            ItemServicoRequest req = new ItemServicoRequest(2, 1, new BigDecimal("0.5"));
            assertThrows(RegraDeNegocioException.class, () -> ordemServicoService.adicionarServico(100, req, adminAuth));
        }
    }

    @Nested
    @DisplayName("N12 - Validação Algorítmica de CPF (@CPF)")
    class ValidacaoCpfTests {

        @Test
        @DisplayName("Rejeita CPF com dígitos verificadores inválidos")
        void cpfInvalido_FalhaValidacao() {
            ClienteRequest req = new ClienteRequest();
            req.setNome("Cliente Teste");
            req.setEndereco("Rua 1");
            req.setCpf("11111111111"); // Inválido por dígitos verificadores

            var violacoes = validator.validate(req);
            assertFalse(violacoes.isEmpty());
            assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cpf")));
        }

        @Test
        @DisplayName("Aceita CPF com algoritmo válido")
        void cpfValido_PassaValidacao() {
            ClienteRequest req = new ClienteRequest();
            req.setNome("Cliente Teste");
            req.setEndereco("Rua 1");
            req.setCpf("52998224725"); // CPF matematicamente válido de teste

            var violacoes = validator.validate(req);
            assertTrue(violacoes.isEmpty(), () -> "Violações encontradas: " + violacoes);
        }
    }
}
