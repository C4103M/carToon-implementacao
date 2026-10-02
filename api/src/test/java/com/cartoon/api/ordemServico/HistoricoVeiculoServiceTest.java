package com.cartoon.api.ordemServico;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.ItemServico;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.servico.Servico;
import com.cartoon.api.usuario.Role;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoVeiculoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    private Veiculo veiculo;
    private Oficina oficina;
    private Usuario mecanico;
    private OrdemServico ordemRecente;
    private OrdemServico ordemAntiga;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNome("João da Silva");
        cliente.setCpf("12345678901");

        veiculo = new Veiculo();
        veiculo.setId(1);
        veiculo.setPlaca("XYZ-9876");
        veiculo.setModelo("Ka");
        veiculo.setMontadora("Ford");
        veiculo.setAno(2019);
        veiculo.setValorFipe(null);
        veiculo.setAtivo(true);
        veiculo.setCliente(cliente);

        oficina = new Oficina();
        oficina.setId(1);
        oficina.setNome("Oficina Central");

        mecanico = new Usuario();
        mecanico.setId(1);
        mecanico.setNome("Carlos Mecânico");
        mecanico.setRole(Role.MECANICO);

        Servico servico = new Servico();
        servico.setId(1);
        servico.setNome("Troca de Óleo");
        servico.setValorBase(100.0);

        Peca peca = new Peca();
        peca.setId(1);
        peca.setNome("Filtro de Óleo");
        peca.setFabricante("Bosch");
        peca.setValorBase(BigDecimal.valueOf(50.0));

        // Ordem Recente (01/10/2026)
        ordemRecente = new OrdemServico();
        ordemRecente.setId(10);
        ordemRecente.setDescricao("Revisão de 50.000 km");
        ordemRecente.setDataOrcamento(LocalDate.of(2026, 10, 1));
        ordemRecente.setDataInicio(LocalDate.of(2026, 10, 2));
        ordemRecente.setDataFinalizacao(LocalDate.of(2026, 10, 3));
        ordemRecente.setStatusServico(StatusServico.FINALIZADO);
        ordemRecente.setVeiculo(veiculo);
        ordemRecente.setOficina(oficina);
        ordemRecente.setMecanico(mecanico);
        ordemRecente.setTotal(BigDecimal.valueOf(350.0));

        ItemServico itemServico = new ItemServico();
        itemServico.setId(1);
        itemServico.setServico(servico);
        itemServico.setQuantidade(1);
        itemServico.setValorUnitario(100.0);
        itemServico.setSubtotal(100.0);
        itemServico.setTempo(LocalTime.of(1, 0));
        ordemRecente.adicionarItemServico(itemServico);

        ItemPeca itemPeca = new ItemPeca();
        itemPeca.setId(1);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(1);
        itemPeca.setValorUnitario(BigDecimal.valueOf(50.0));
        itemPeca.setSubtotal(BigDecimal.valueOf(50.0));
        ordemRecente.adicionarItemPeca(itemPeca);

        // Ordem Antiga (15/05/2026)
        ordemAntiga = new OrdemServico();
        ordemAntiga.setId(5);
        ordemAntiga.setDescricao("Alinhamento e Balanceamento");
        ordemAntiga.setDataOrcamento(LocalDate.of(2026, 5, 15));
        ordemAntiga.setStatusServico(StatusServico.FINALIZADO);
        ordemAntiga.setVeiculo(veiculo);
        ordemAntiga.setOficina(oficina);
        ordemAntiga.setMecanico(mecanico);
        ordemAntiga.setTotal(BigDecimal.valueOf(120.0));
    }

    @Test
    @DisplayName("UC06: Buscar histórico por placa retorna OrdemServicoResumo ordenado por data")
    void deveBuscarHistoricoPorPlaca() {
        when(ordemServicoRepository.findByVeiculoPlacaIgnoreCaseOrderByDataOrcamentoDescIdDesc("XYZ-9876"))
                .thenReturn(List.of(ordemRecente, ordemAntiga));

        List<OrdemServicoResumo> ordens = ordemServicoService.buscarHistoricoPorPlaca("XYZ-9876");

        assertThat(ordens).hasSize(2);
        assertThat(ordens.get(0).id()).isEqualTo(10);
        assertThat(ordens.get(0).placa()).isEqualTo("XYZ-9876");
        assertThat(ordens.get(0).mecanicoNome()).isEqualTo("Carlos Mecânico");
        assertThat(ordens.get(0).oficinaNome()).isEqualTo("Oficina Central");
        assertThat(ordens.get(0).valorTotal()).isEqualTo(BigDecimal.valueOf(350.0));

        assertThat(ordens.get(1).id()).isEqualTo(5);
        assertThat(ordens.get(1).dataOrcamento()).isEqualTo(LocalDate.of(2026, 5, 15));
    }

    @Test
    @DisplayName("UC06: Buscar detalhes de uma ordem existente através de buscar(id)")
    void deveBuscarDetalhesDaOrdemPorId() {
        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemRecente));

        OrdemServicoResponse response = ordemServicoService.buscar(10);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10);
        assertThat(response.descricao()).isEqualTo("Revisão de 50.000 km");
        assertThat(response.mecanicoNome()).isEqualTo("Carlos Mecânico");
        assertThat(response.itensPeca()).hasSize(1);
        assertThat(response.itensPeca().get(0).pecaNome()).isEqualTo("Filtro de Óleo");
    }

    @Test
    @DisplayName("UC06: Buscar ordem inexistente lança RecursoNaoEncontradoException")
    void deveLancarExcecaoQuandoOrdemNaoEncontrada() {
        when(ordemServicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordemServicoService.buscar(99))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Ordem de servico não encontrado(a): 99");
    }

    @Test
    @DisplayName("UC06: Listar ordens paginadas com filtro por placa")
    void deveListarOrdensPaginadasComFiltroPlaca() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrdemServico> page = new PageImpl<>(List.of(ordemRecente), pageable, 1);

        when(ordemServicoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        OrdemServicoFiltro filtro = new OrdemServicoFiltro(null, null, "XYZ-9876", null, null);
        Page<OrdemServicoResumo> resultado = ordemServicoService.listar(filtro, pageable);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).id()).isEqualTo(10);
        assertThat(resultado.getContent().get(0).placa()).isEqualTo("XYZ-9876");
    }
}
