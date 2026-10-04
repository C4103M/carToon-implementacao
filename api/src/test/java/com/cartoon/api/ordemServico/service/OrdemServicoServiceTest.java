package com.cartoon.api.ordemServico.service;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.oficina.OficinaService;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.ItemPeca;
import com.cartoon.api.ordemServico.models.OrdemServico;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repositories.OrdemServicoRepository;
import com.cartoon.api.peca.Peca;
import com.cartoon.api.peca.PecaService;
import com.cartoon.api.usuario.Role;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioService;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoService;
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
    @DisplayName("Salvar - Deve criar e retornar nova Ordem de Serviço")
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

        when(ordemServicoRepository.findById(10)).thenReturn(Optional.of(ordemServico));

        OrdemServicoResponse response = ordemServicoService.removerPeca(10, 100);

        assertNotNull(response);
        assertTrue(ordemServico.getItensPeca().isEmpty());
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
}
