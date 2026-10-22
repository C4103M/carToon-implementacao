package com.cartoon.api.fipe;

import com.cartoon.api.compartilhado.exceptions.FipeIndisponivelException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.compartilhado.exceptions.TipoVeiculoInvalidoException;
import com.cartoon.api.fipe.client.FipeClient;
import com.cartoon.api.fipe.dto.FipeItemDTO;
import com.cartoon.api.fipe.dto.FipeItemResponse;
import com.cartoon.api.fipe.dto.FipeValorDTO;
import com.cartoon.api.fipe.dto.FipeVeiculoResponse;
import com.cartoon.api.fipe.dto.TipoVeiculo;
import com.cartoon.api.fipe.service.FipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FipeServiceTest {

    @Mock
    private FipeClient fipeClient;

    @InjectMocks
    private FipeService fipeService;

    @Test
    @DisplayName("Deve listar marcas mapeando code/name para codigo/nome")
    void deveListarMarcasComSucesso() {
        when(fipeClient.buscarMarcas("cars")).thenReturn(List.of(
                new FipeItemResponse("21", "Fiat"),
                new FipeItemResponse("59", "VW - VolksWagen")
        ));

        List<FipeItemDTO> resultado = fipeService.listarMarcas(TipoVeiculo.CARROS);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).codigo()).isEqualTo("21");
        assertThat(resultado.get(0).nome()).isEqualTo("Fiat");
        verify(fipeClient).buscarMarcas("cars");
    }

    @Test
    @DisplayName("Deve consultar valor e converter preço 'R$ 21.500,00' em BigDecimal")
    void deveConsultarValorEConverterPreco() {
        FipeVeiculoResponse response = new FipeVeiculoResponse(
                1,
                "R$ 21.500,00",
                "Fiat",
                "Uno Mille 1.0",
                2010,
                "Gasolina",
                "001004-9",
                "outubro de 2026",
                "G"
        );

        when(fipeClient.buscarValor("cars", "21", "4828", "2010-1")).thenReturn(response);

        FipeValorDTO valor = fipeService.consultarValor(TipoVeiculo.CARROS, "21", "4828", "2010-1");

        assertThat(valor).isNotNull();
        assertThat(valor.marca()).isEqualTo("Fiat");
        assertThat(valor.modelo()).isEqualTo("Uno Mille 1.0");
        assertThat(valor.anoModelo()).isEqualTo(2010);
        assertThat(valor.combustivel()).isEqualTo("Gasolina");
        assertThat(valor.siglaCombustivel()).isEqualTo("G");
        assertThat(valor.codigoFipe()).isEqualTo("001004-9");
        assertThat(valor.valorFipe()).isEqualByComparingTo(new BigDecimal("21500.00"));
        assertThat(valor.mesReferencia()).isEqualTo("outubro de 2026");
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException (404) quando a FIPE retornar 404")
    void deveLancarRecursoNaoEncontradoQuandoFipeRetornar404() {
        when(fipeClient.buscarModelos("cars", "99999"))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8
                ));

        assertThatThrownBy(() -> fipeService.listarModelos(TipoVeiculo.CARROS, "99999"))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar FipeIndisponivelException (503) quando a FIPE falhar ou estourar timeout")
    void deveLancarFipeIndisponivelQuandoOcorrerErroDeRede() {
        when(fipeClient.buscarMarcas("motorcycles"))
                .thenThrow(new ResourceAccessException("Read timed out"));

        assertThatThrownBy(() -> fipeService.listarMarcas(TipoVeiculo.MOTOS))
                .isInstanceOf(FipeIndisponivelException.class);
    }

    @Test
    @DisplayName("TipoVeiculo deve converter variações válidas e rejeitar inválidas com TipoVeiculoInvalidoException")
    void deveConverterTipoVeiculoCorretamente() {
        assertThat(TipoVeiculo.from("carro")).isEqualTo(TipoVeiculo.CARROS);
        assertThat(TipoVeiculo.from("CARROS")).isEqualTo(TipoVeiculo.CARROS);
        assertThat(TipoVeiculo.from("motos")).isEqualTo(TipoVeiculo.MOTOS);
        assertThat(TipoVeiculo.from("caminhões")).isEqualTo(TipoVeiculo.CAMINHOES);

        assertThatThrownBy(() -> TipoVeiculo.from("aviao"))
                .isInstanceOf(TipoVeiculoInvalidoException.class);
    }
}
