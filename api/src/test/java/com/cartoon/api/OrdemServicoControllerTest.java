package com.cartoon.api;

import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.ordemServico.controller.OrdemServicoController;
import com.cartoon.api.ordemServico.dto.OrdemServicoFiltro;
import com.cartoon.api.ordemServico.dto.OrdemServicoResumo;
import com.cartoon.api.ordemServico.dto.request.OrdemServicoRequest;
import com.cartoon.api.ordemServico.dto.response.OrdemServicoResponse;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.service.OrdemServicoService;
import com.cartoon.api.seguranca.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdemServicoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @Test
    @DisplayName("GET /api/ordem-servico com filtro por placa retorna Page<OrdemServicoResumo>")
    void deveListarOrdensComFiltroPorPlaca() throws Exception {
        OrdemServicoResumo resumo = new OrdemServicoResumo(
                10,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 3),
                null,
                StatusServico.FINALIZADO,
                "Revisão preventiva",
                "XYZ-9876",
                "Carlos Mecânico",
                "Oficina Central",
                BigDecimal.valueOf(150.0)
        );

        Page<OrdemServicoResumo> page = new PageImpl<>(List.of(resumo), PageRequest.of(0, 10), 1);

        when(ordemServicoService.listar(any(OrdemServicoFiltro.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/ordem-servico")
                        .param("placa", "XYZ-9876"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].placa").value("XYZ-9876"))
                .andExpect(jsonPath("$.content[0].mecanicoNome").value("Carlos Mecânico"))
                .andExpect(jsonPath("$.content[0].statusServico").value("FINALIZADO"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/ordem-servico/{id} retorna ordem de serviço por ID")
    void deveBuscarOrdemPorId() throws Exception {
        OrdemServicoResponse response = new OrdemServicoResponse(
                1,
                StatusServico.PENDENTE,
                "Troca de óleo",
                1,
                "XYZ-9876",
                1,
                1,
                "Carlos Mecânico",
                List.of(),
                BigDecimal.valueOf(100.0)
        );

        when(ordemServicoService.buscar(1)).thenReturn(response);

        mockMvc.perform(get("/api/ordem-servico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.placa").value("XYZ-9876"));
    }

    @Test
    @DisplayName("GET /api/ordem-servico/{id} inexistente retorna 404 Not Found com ErroResposta DTO")
    void deveRetornar404QuandoOrdemInexistente() throws Exception {
        when(ordemServicoService.buscar(999))
                .thenThrow(new RecursoNaoEncontradoException("Ordem de servico", 999));

        mockMvc.perform(get("/api/ordem-servico/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Ordem de servico não encontrado(a): 999"));
    }

    @Test
    @DisplayName("POST /api/ordem-servico cria ordem e retorna 201 Created")
    void deveCriarOrdemServico() throws Exception {
        OrdemServicoRequest request = new OrdemServicoRequest(
                1,
                1,
                1,
                "Revisão 10k"
        );

        OrdemServicoResponse response = new OrdemServicoResponse(
                1,
                StatusServico.PENDENTE,
                "Revisão 10k",
                1,
                "XYZ-9876",
                1,
                1,
                "Carlos Mecânico",
                List.of(),
                BigDecimal.ZERO
        );

        when(ordemServicoService.salvar(any(OrdemServicoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/ordem-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ordem-servico/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.descricao").value("Revisão 10k"));
    }
}
