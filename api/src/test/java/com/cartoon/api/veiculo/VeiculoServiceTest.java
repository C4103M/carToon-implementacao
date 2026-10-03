package com.cartoon.api.veiculo;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.PlacaInvalidaException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoFiltro;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private VeiculoService veiculoService;

    private Cliente clienteValido;
    private Veiculo veiculoValido;

    @BeforeEach
    void setUp() {
        clienteValido = new Cliente();
        clienteValido.setId(1);
        clienteValido.setNome("João da Silva");
        clienteValido.setCpf("12345678901");
        clienteValido.setAtivo(true);

        veiculoValido = new Veiculo();
        veiculoValido.setId(1);
        veiculoValido.setPlaca("XYZ-9876");
        veiculoValido.setMontadora("Ford");
        veiculoValido.setModelo("Ka");
        veiculoValido.setAno(2019);
        veiculoValido.setValorFipe(null);
        veiculoValido.setAtivo(true);
        veiculoValido.setCliente(clienteValido);
    }

    @Test
    @DisplayName("UC05 - Teste 01: Cadastrar veículo válido com sucesso e valor FIPE null")
    void deveCadastrarVeiculoValido() {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);
        when(veiculoRepository.existsByPlacaIgnoreCase("XYZ-9876")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo v = invocation.getArgument(0);
            v.setId(1);
            return v;
        });

        VeiculoResponse response = veiculoService.cadastrar(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.placa()).isEqualTo("XYZ-9876");
        assertThat(response.montadora()).isEqualTo("Ford");
        assertThat(response.modelo()).isEqualTo("Ka");
        assertThat(response.ano()).isEqualTo(2019);
        assertThat(response.valorFipe()).isNull();
        assertThat(response.clienteId()).isEqualTo(1);
        assertThat(response.clienteNome()).isEqualTo("João da Silva");


        verify(clienteService).buscarEntidade(1);
        verify(veiculoRepository).existsByPlacaIgnoreCase("XYZ-9876");
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("UC05 - Teste 01 (Mercosul): Cadastrar veículo com placa no padrão Mercosul")
    void deveCadastrarVeiculoPlacaMercosul() {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "ABC1D23",
                "Volkswagen",
                "Gol 1.0",
                2020
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);
        when(veiculoRepository.existsByPlacaIgnoreCase("ABC1D23")).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo v = invocation.getArgument(0);
            v.setId(2);
            return v;
        });

        VeiculoResponse response = veiculoService.cadastrar(request);

        assertThat(response).isNotNull();
        assertThat(response.placa()).isEqualTo("ABC1D23");
        assertThat(response.valorFipe()).isNull();

    }

    @Test
    @DisplayName("UC05 - Teste 02: Impedir cadastro de placa duplicada")
    void deveBloquearCadastroDePlacaDuplicada() {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);
        when(veiculoRepository.existsByPlacaIgnoreCase("XYZ-9876")).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe um veículo cadastrado com esta placa no sistema.");

        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("UC05 - Teste 03: Editar dados do veículo mantendo cliente")
    void deveEditarDadosDoVeiculoMantendoCliente() {
        VeiculoAtualizacaoRequest request = new VeiculoAtualizacaoRequest(
                "Ford Atualizada",
                "Ka Sedan",
                2020,
                null,
                null,
                null
        );

        when(veiculoRepository.findById(1)).thenReturn(Optional.of(veiculoValido));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeiculoResponse response = veiculoService.atualizar(1, request);

        assertThat(response).isNotNull();
        assertThat(response.montadora()).isEqualTo("Ford Atualizada");
        assertThat(response.modelo()).isEqualTo("Ka Sedan");
        assertThat(response.ano()).isEqualTo(2020);
        assertThat(response.valorFipe()).isNull();
        assertThat(response.clienteNome()).isEqualTo("João da Silva");


        verify(veiculoRepository).save(veiculoValido);
    }

    @Test
    @DisplayName("UC05 - Teste 04: Validar formato da placa - placa fora do padrão 'ABC-12'")
    void deveBloquearPlacaInvalidaABC12() {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "ABC-12",
                "Ford",
                "Ka",
                2019
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(PlacaInvalidaException.class)
                .hasMessage("Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul.");

        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("UC05 - Teste 04: Validar formato da placa - placa fora do padrão '123-ABCD'")
    void deveBloquearPlacaInvalida123ABCD() {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "123-ABCD",
                "Ford",
                "Ka",
                2019
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(PlacaInvalidaException.class)
                .hasMessage("Formato de placa inválido. Utilize o padrão AAA-1234 ou Mercosul.");

        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("UC05: Bloquear cadastro para cliente inativo")
    void deveBloquearCadastroParaClienteInativo() {
        clienteValido.setAtivo(false);
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019
        );

        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);

        assertThatThrownBy(() -> veiculoService.cadastrar(request))
                .isInstanceOf(ClienteInativoException.class)
                .hasMessage("Cliente inativo no sistema.");

        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("UC05: Inativar veículo com sucesso")
    void deveInativarVeiculo() {
        when(veiculoRepository.findById(1)).thenReturn(Optional.of(veiculoValido));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(i -> i.getArgument(0));

        VeiculoResponse response = veiculoService.inativar(1);

        assertThat(response).isNotNull();
        assertThat(response.ativo()).isFalse();
        assertThat(veiculoValido.getAtivo()).isFalse();
        verify(veiculoRepository).save(veiculoValido);
    }

    @Test
    @DisplayName("UC05: Reativar veículo com sucesso")
    void deveReativarVeiculo() {
        veiculoValido.setAtivo(false);
        when(veiculoRepository.findById(1)).thenReturn(Optional.of(veiculoValido));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(i -> i.getArgument(0));

        VeiculoResponse response = veiculoService.reativar(1);

        assertThat(response).isNotNull();
        assertThat(response.ativo()).isTrue();
        assertThat(veiculoValido.getAtivo()).isTrue();
        verify(veiculoRepository).save(veiculoValido);
    }

    @Test
    @DisplayName("UC05: Buscar veículo por ID com sucesso")
    void deveBuscarVeiculoPorId() {
        when(veiculoRepository.findComClienteById(1)).thenReturn(Optional.of(veiculoValido));

        VeiculoResponse response = veiculoService.buscarPorId(1);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1);
        assertThat(response.placa()).isEqualTo("XYZ-9876");
    }

    @Test
    @DisplayName("UC05: Buscar veículo por placa com sucesso")
    void deveBuscarVeiculoPorPlaca() {
        when(veiculoRepository.findComClienteByPlacaIgnoreCase("XYZ-9876")).thenReturn(Optional.of(veiculoValido));

        VeiculoResponse response = veiculoService.buscarPorPlaca("XYZ-9876");

        assertThat(response).isNotNull();
        assertThat(response.placa()).isEqualTo("XYZ-9876");
    }

    @Test
    @DisplayName("UC05: Buscar veículo por placa inexistente lança exceção")
    void deveLancarExcecaoQuandoVeiculoNaoEncontradoPorPlaca() {
        when(veiculoRepository.findComClienteByPlacaIgnoreCase("NAO-0000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.buscarPorPlaca("NAO-0000"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado.");
    }

    @Test
    @DisplayName("UC05: Listar veículos com filtros e paginação")
    void deveListarVeiculosComFiltros() {
        VeiculoFiltro filtro = new VeiculoFiltro(1, "XYZ-9876", "Ka", "Ford", true);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Veiculo> page = new PageImpl<>(List.of(veiculoValido));

        when(veiculoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<VeiculoResponse> resultado = veiculoService.listar(filtro, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).placa()).isEqualTo("XYZ-9876");
    }

    @Test
    @DisplayName("UC05: Listar veículos por cliente")
    void deveListarVeiculosPorCliente() {
        when(clienteService.buscarEntidade(1)).thenReturn(clienteValido);
        when(veiculoRepository.findByClienteId(1)).thenReturn(List.of(veiculoValido));

        List<VeiculoResponse> lista = veiculoService.listarPorCliente(1);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).clienteId()).isEqualTo(1);
    }

    @Test
    @DisplayName("UC06: Consultar histórico de ordens via VeiculoService acessando OrdemServicoService")
    void deveConsultarHistoricoVeicular() {
        when(veiculoRepository.findComClienteByPlacaIgnoreCase("XYZ-9876")).thenReturn(Optional.of(veiculoValido));

        OrdemServicoResumo osResumo = new OrdemServicoResumo(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão 50k",
                "XYZ-9876",
                "Carlos",
                "Oficina Central",
                BigDecimal.valueOf(350.0)
        );

        when(ordemServicoService.buscarHistoricoPorPlaca("XYZ-9876")).thenReturn(List.of(osResumo));

        HistoricoVeiculoResponse historico = veiculoService.consultarHistorico("XYZ-9876");

        assertThat(historico).isNotNull();
        assertThat(historico.veiculo().placa()).isEqualTo("XYZ-9876");
        assertThat(historico.totalOrdens()).isEqualTo(1);
        assertThat(historico.ordensServico().get(0).id()).isEqualTo(10);
        assertThat(historico.ordensServico().get(0).valorTotal()).isEqualTo(BigDecimal.valueOf(350.0));
    }
}
