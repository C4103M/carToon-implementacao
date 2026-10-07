package com.cartoon.api.veiculo;

import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.seguranca.SecurityConfig;
import com.cartoon.api.veiculo.dto.request.VeiculoAtualizacaoRequest;
import com.cartoon.api.veiculo.dto.request.VeiculoRequest;
import com.cartoon.api.veiculo.dto.response.HistoricoVeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResponse;
import com.cartoon.api.veiculo.dto.response.VeiculoResumo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VeiculoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private VeiculoService veiculoService;

    @MockitoBean
    private com.cartoon.api.seguranca.JwtService jwtService;

    @Test
    @DisplayName("UC05 - Teste 01: POST /api/veiculos cadastra veículo válido e retorna 201 Created com valor FIPE null")
    void deveCadastrarVeiculoComSucesso() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019
        );

        VeiculoResponse response = new VeiculoResponse(
                1,
                "XYZ-9876",
                "Ka",
                2019,
                "Ford",
                null,
                true,
                1,
                "João da Silva",
                "12345678901"
        );

        when(veiculoService.cadastrar(any(VeiculoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/veiculos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.montadora").value("Ford"))
                .andExpect(jsonPath("$.modelo").value("Ka"))
                .andExpect(jsonPath("$.ano").value(2019))
                .andExpect(jsonPath("$.valorFipe").doesNotExist())
                .andExpect(jsonPath("$.clienteNome").value("João da Silva"));
    }

    @Test
    @DisplayName("UC05 - Teste 02: POST /api/veiculos com placa duplicada retorna 409 Conflict")
    void deveRetornar409ParaPlacaDuplicada() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "XYZ-9876",
                "Ford",
                "Ka",
                2019
        );

        when(veiculoService.cadastrar(any(VeiculoRequest.class)))
                .thenThrow(new ConflitoException("Já existe um veículo cadastrado com esta placa no sistema."));

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Já existe um veículo cadastrado com esta placa no sistema."));
    }

    @Test
    @DisplayName("UC05 - Teste 03: PUT /api/veiculos/{id} atualiza dados do veículo e retorna 200 OK")
    void deveAtualizarDadosDoVeiculo() throws Exception {
        VeiculoAtualizacaoRequest request = new VeiculoAtualizacaoRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        VeiculoResponse response = new VeiculoResponse(
                1,
                "XYZ-9876",
                "Ka",
                2019,
                "Ford",
                null,
                true,
                1,
                "João da Silva",
                "12345678901"
        );

        when(veiculoService.atualizar(eq(1), any(VeiculoAtualizacaoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placa").value("XYZ-9876"));
    }

    @Test
    @DisplayName("UC05 - Teste 04: POST /api/veiculos com placa inválida retorna 400 Bad Request")
    void deveRetornar400ParaPlacaInvalida() throws Exception {
        VeiculoRequest request = new VeiculoRequest(
                1,
                "ABC-12",
                "Ford",
                "Ka",
                2019
        );

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("UC05: PATCH /api/veiculos/{id}/inativar inativa veículo e retorna 200 OK com DTO")
    void deveInativarVeiculo() throws Exception {
        VeiculoResponse response = new VeiculoResponse(
                1, "XYZ-9876", "Ka", 2019, "Ford", null, false, 1, "João", "12345678901"
        );
        when(veiculoService.inativar(1)).thenReturn(response);

        mockMvc.perform(patch("/api/veiculos/1/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @DisplayName("UC06 - Teste 01: GET /api/veiculos/{placa}/historico retorna ordens da mais recente para a mais antiga")
    void deveConsultarHistoricoComSucesso() throws Exception {
        VeiculoResumo veiculoResumo = new VeiculoResumo(1, "XYZ-9876", "Ka", "Ford", 2019, null, true, "João da Silva");
        OrdemServicoResumo ordem = new OrdemServicoResumo(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão de 50.000 km",
                "XYZ-9876",
                "Carlos Mecânico",
                "Oficina Central",
                BigDecimal.valueOf(350.0)
        );

        HistoricoVeiculoResponse historico = new HistoricoVeiculoResponse(
                veiculoResumo,
                List.of(ordem),
                1
        );

        when(veiculoService.consultarHistorico("XYZ-9876")).thenReturn(historico);

        mockMvc.perform(get("/api/veiculos/XYZ-9876/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.totalOrdens").value(1))
                .andExpect(jsonPath("$.ordensServico[0].id").value(10))
                .andExpect(jsonPath("$.ordensServico[0].mecanicoNome").value("Carlos Mecânico"))
                .andExpect(jsonPath("$.ordensServico[0].valorTotal").value(350.0));
    }

    @Test
    @DisplayName("UC06 - Teste 02: GET /api/veiculos/{placa}/historico para veículo sem ordens")
    void deveConsultarHistoricoVeiculoSemOrdens() throws Exception {
        VeiculoResumo veiculoResumo = new VeiculoResumo(1, "XYZ-9876", "Ka", "Ford", 2019, null, true, "João da Silva");
        HistoricoVeiculoResponse historico = new HistoricoVeiculoResponse(
                veiculoResumo,
                List.of(),
                0
        );

        when(veiculoService.consultarHistorico("XYZ-9876")).thenReturn(historico);

        mockMvc.perform(get("/api/veiculos/XYZ-9876/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veiculo.placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.ordensServico").isEmpty());
    }

    @Test
    @DisplayName("UC06 - Teste 03: GET /api/veiculos/{placa}/historico para placa inexistente retorna 404")
    void deveRetornar404ParaPlacaInexistenteNoHistorico() throws Exception {
        when(veiculoService.consultarHistorico("NAO-0000"))
                .thenThrow(new RecursoNaoEncontradoException("Veículo", "NAO-0000"));

        mockMvc.perform(get("/api/veiculos/NAO-0000/historico"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
