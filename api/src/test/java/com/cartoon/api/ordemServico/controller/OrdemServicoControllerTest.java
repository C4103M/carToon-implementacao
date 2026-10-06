package com.cartoon.api.ordemServico.controller;

import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.ItemPecaRequest;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private OrdemServicoController ordemServicoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrdemServicoResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ordemServicoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = new OrdemServicoResponse(
                1,
                StatusServico.PENDENTE,
                null,
                null,
                null,
                null,
                "Troca de filtro",
                10,
                "ABC-1234",
                100,
                5,
                "Mecanico Joao",
                Collections.emptyList(),
                BigDecimal.valueOf(150.00)
        );
    }

    @Test
    @DisplayName("POST /ordem-servico - Deve criar Ordem de Serviço e retornar 201 Created com Header Location")
    void salvar_ComDadosValidos_DeveRetornar201Created() throws Exception {
        OrdemServicoRequest request = new OrdemServicoRequest(10, 100, 5, "Troca de filtro");

        when(ordemServicoService.salvar(any(OrdemServicoRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/ordem-servico/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.descricao", is("Troca de filtro")))
                .andExpect(jsonPath("$.status", is("PENDENTE")));
    }

    @Test
    @DisplayName("POST /ordem-servico - Deve retornar 400 Bad Request ao passar dados inválidos")
    void salvar_ComDadosInvalidos_DeveRetornar400BadRequest() throws Exception {
        // request sem veiculoId obrigatório
        OrdemServicoRequest requestInvalido = new OrdemServicoRequest(null, 100, 5, "Troca de filtro");

        mockMvc.perform(post("/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /ordem-servico/{id} - Deve buscar Ordem de Serviço por ID e retornar 200 OK")
    void buscar_DeveRetornar200OK() throws Exception {
        when(ordemServicoService.buscar(1)).thenReturn(sampleResponse);

        mockMvc.perform(get("/ordem-servico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.placa", is("ABC-1234")));
    }

    @Test
    @DisplayName("GET /ordem-servico - Deve listar Ordens de Serviço paginadas e retornar 200 OK")
    void listar_DeveRetornarPaginaResumo() throws Exception {
        OrdemServicoResumo resumo = new OrdemServicoResumo(1, StatusServico.PENDENTE, "ABC-1234", "Mecanico Joao");
        PageImpl<OrdemServicoResumo> page = new PageImpl<>(List.of(resumo), PageRequest.of(0, 10), 1);

        when(ordemServicoService.listar(any(), any())).thenReturn(page);

        mockMvc.perform(get("/ordem-servico")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].placa", is("ABC-1234")));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/aceitar - Deve aceitar Ordem de Serviço e retornar 200 OK")
    void aceitar_DeveRetornar200OK() throws Exception {
        OrdemServicoResponse aceitoResponse = new OrdemServicoResponse(
                1, StatusServico.ACEITO, null,null,null,null,"Troca de filtro", 10, "ABC-1234", 100, 5, "Mecanico Joao", Collections.emptyList(), BigDecimal.valueOf(150.00));

        when(ordemServicoService.aceitar(1)).thenReturn(aceitoResponse);

        mockMvc.perform(post("/ordem-servico/1/aceitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACEITO")));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/iniciar - Deve iniciar Ordem de Serviço e retornar 200 OK")
    void iniciar_DeveRetornar200OK() throws Exception {
        OrdemServicoResponse emAndamentoResponse = new OrdemServicoResponse(
                1, StatusServico.EM_ANDAMENTO,null,null,null,null, "Troca de filtro", 10, "ABC-1234", 100, 5, "Mecanico Joao", Collections.emptyList(), BigDecimal.valueOf(150.00));

        when(ordemServicoService.iniciar(1)).thenReturn(emAndamentoResponse);

        mockMvc.perform(post("/ordem-servico/1/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EM_ANDAMENTO")));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/finalizar - Deve finalizar Ordem de Serviço e retornar 200 OK")
    void finalizar_DeveRetornar200OK() throws Exception {
        OrdemServicoResponse finalizadoResponse = new OrdemServicoResponse(
                1, StatusServico.FINALIZADO,null,null,null,null, "Troca de filtro", 10, "ABC-1234", 100, 5, "Mecanico Joao", Collections.emptyList(), BigDecimal.valueOf(150.00));

        when(ordemServicoService.finalizar(1)).thenReturn(finalizadoResponse);

        mockMvc.perform(post("/ordem-servico/1/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FINALIZADO")));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/rejeitar - Deve rejeitar Ordem de Serviço e retornar 200 OK")
    void rejeitar_DeveRetornar200OK() throws Exception {
        OrdemServicoResponse rejeitadoResponse = new OrdemServicoResponse(
                1, StatusServico.REJEITADO,null,null,null,null, "Troca de filtro", 10, "ABC-1234", 100, 5, "Mecanico Joao", Collections.emptyList(), BigDecimal.valueOf(150.00));

        when(ordemServicoService.rejeitar(1)).thenReturn(rejeitadoResponse);

        mockMvc.perform(post("/ordem-servico/1/rejeitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJEITADO")));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/itens-peca - Deve adicionar item de peça à Ordem de Serviço")
    void adicionarPeca_DeveRetornar200OK() throws Exception {
        ItemPecaRequest request = new ItemPecaRequest(50, 2);

        when(ordemServicoService.adicionarPeca(eq(1), any(ItemPecaRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/ordem-servico/1/itens-peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("POST /ordem-servico/{id}/itens-peca - Deve retornar 400 Bad Request ao enviar quantidade inválida (<= 0)")
    void adicionarPeca_ComQuantidadeInvalida_DeveRetornar400BadRequest() throws Exception {
        ItemPecaRequest requestInvalido = new ItemPecaRequest(50, 0);

        mockMvc.perform(post("/ordem-servico/1/itens-peca")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /ordem-servico/{id}/itens-peca/{itemId} - Deve remover item de peça da Ordem de Serviço")
    void removerPeca_DeveRetornar200OK() throws Exception {
        when(ordemServicoService.removerPeca(1, 100)).thenReturn(sampleResponse);

        mockMvc.perform(delete("/ordem-servico/1/itens-peca/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }
}
