package com.cartoon.api.fipe;

import com.cartoon.api.compartilhado.exceptions.FipeIndisponivelException;
import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.fipe.controller.FipeController;
import com.cartoon.api.fipe.dto.FipeItemDTO;
import com.cartoon.api.fipe.dto.FipeValorDTO;
import com.cartoon.api.fipe.dto.TipoVeiculo;
import com.cartoon.api.fipe.service.FipeService;
import com.cartoon.api.seguranca.JwtService;
import com.cartoon.api.seguranca.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FipeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class FipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FipeService fipeService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /fipe/{tipo}/marcas retorna 200 OK com lista de marcas")
    void deveRetornarMarcas() throws Exception {
        when(fipeService.listarMarcas(TipoVeiculo.CARROS))
                .thenReturn(List.of(new FipeItemDTO("21", "Fiat")));

        mockMvc.perform(get("/fipe/carros/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("21"))
                .andExpect(jsonPath("$[0].nome").value("Fiat"));
    }

    @Test
    @DisplayName("GET /fipe/{tipo}/marcas/{marcaId}/modelos retorna 200 OK com lista de modelos")
    void deveRetornarModelos() throws Exception {
        when(fipeService.listarModelos(TipoVeiculo.CARROS, "21"))
                .thenReturn(List.of(new FipeItemDTO("4828", "Uno Mille 1.0")));

        mockMvc.perform(get("/fipe/carros/marcas/21/modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("4828"))
                .andExpect(jsonPath("$[0].nome").value("Uno Mille 1.0"));
    }

    @Test
    @DisplayName("GET /fipe/{tipo}/marcas/{marcaId}/modelos/{modeloId}/anos retorna 200 OK com lista de anos")
    void deveRetornarAnos() throws Exception {
        when(fipeService.listarAnos(TipoVeiculo.CARROS, "21", "4828"))
                .thenReturn(List.of(new FipeItemDTO("2010-1", "2010 Gasolina")));

        mockMvc.perform(get("/fipe/carros/marcas/21/modelos/4828/anos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("2010-1"))
                .andExpect(jsonPath("$[0].nome").value("2010 Gasolina"));
    }

    @Test
    @DisplayName("GET /fipe/{tipo}/marcas/{marcaId}/modelos/{modeloId}/anos/{anoId} retorna 200 OK com FipeValorDTO")
    void deveRetornarValorFipe() throws Exception {
        FipeValorDTO dto = new FipeValorDTO(
                "Fiat",
                "Uno Mille 1.0",
                2010,
                "Gasolina",
                "G",
                "001004-9",
                new BigDecimal("21500.00"),
                "outubro de 2026"
        );

        when(fipeService.consultarValor(TipoVeiculo.CARROS, "21", "4828", "2010-1"))
                .thenReturn(dto);

        mockMvc.perform(get("/fipe/carros/marcas/21/modelos/4828/anos/2010-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Fiat"))
                .andExpect(jsonPath("$.modelo").value("Uno Mille 1.0"))
                .andExpect(jsonPath("$.anoModelo").value(2010))
                .andExpect(jsonPath("$.combustivel").value("Gasolina"))
                .andExpect(jsonPath("$.codigoFipe").value("001004-9"))
                .andExpect(jsonPath("$.valorFipe").value(21500.00))
                .andExpect(jsonPath("$.mesReferencia").value("outubro de 2026"));
    }

    @Test
    @DisplayName("GET /fipe/{tipo}/marcas com tipo inválido retorna 400 Bad Request")
    void deveRetornar400QuandoTipoInvalido() throws Exception {
        mockMvc.perform(get("/fipe/barcos/marcas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /fipe/... com marca/modelo/ano inexistente retorna 404 Not Found")
    void deveRetornar404QuandoRecursoNaoEncontrado() throws Exception {
        when(fipeService.listarModelos(TipoVeiculo.CARROS, "999"))
                .thenThrow(new RecursoNaoEncontradoException("Marca não encontrada na tabela FIPE: 999"));

        mockMvc.perform(get("/fipe/carros/marcas/999/modelos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /fipe/... quando FIPE está fora do ar retorna 503 Service Unavailable")
    void deveRetornar503QuandoFipeIndisponivel() throws Exception {
        when(fipeService.listarMarcas(TipoVeiculo.CARROS))
                .thenThrow(new FipeIndisponivelException());

        mockMvc.perform(get("/fipe/carros/marcas"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}
