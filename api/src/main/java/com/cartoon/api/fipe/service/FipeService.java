package com.cartoon.api.fipe.service;

import com.cartoon.api.compartilhado.exceptions.FipeIndisponivelException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.fipe.client.FipeClient;
import com.cartoon.api.fipe.dto.FipeItemDTO;
import com.cartoon.api.fipe.dto.FipeItemResponse;
import com.cartoon.api.fipe.dto.FipeValorDTO;
import com.cartoon.api.fipe.dto.FipeVeiculoResponse;
import com.cartoon.api.fipe.dto.TipoVeiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FipeService {

    private final FipeClient fipeClient;

    @Cacheable(value = "fipe-marcas", key = "#tipo.name()", sync = true)
    public List<FipeItemDTO> listarMarcas(TipoVeiculo tipo) {
        try {
            List<FipeItemResponse> response = fipeClient.buscarMarcas(tipo.getFipePath());
            return mapearItens(response);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Marcas não encontradas para o tipo informado.");
        } catch (RestClientException ex) {
            throw new FipeIndisponivelException("Não foi possível consultar as marcas na tabela FIPE.", ex);
        }
    }

    @Cacheable(value = "fipe-modelos", key = "#tipo.name() + '-' + #marcaId", sync = true)
    public List<FipeItemDTO> listarModelos(TipoVeiculo tipo, String marcaId) {
        try {
            List<FipeItemResponse> response = fipeClient.buscarModelos(tipo.getFipePath(), marcaId);
            return mapearItens(response);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Marca não encontrada na tabela FIPE: " + marcaId);
        } catch (RestClientException ex) {
            throw new FipeIndisponivelException("Não foi possível consultar os modelos na tabela FIPE.", ex);
        }
    }

    @Cacheable(value = "fipe-anos", key = "#tipo.name() + '-' + #marcaId + '-' + #modeloId", sync = true)
    public List<FipeItemDTO> listarAnos(TipoVeiculo tipo, String marcaId, String modeloId) {
        try {
            List<FipeItemResponse> response = fipeClient.buscarAnos(tipo.getFipePath(), marcaId, modeloId);
            return mapearItens(response);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Marca ou modelo não encontrado na tabela FIPE.");
        } catch (RestClientException ex) {
            throw new FipeIndisponivelException("Não foi possível consultar os anos na tabela FIPE.", ex);
        }
    }

    @Cacheable(value = "fipe-valor", key = "#tipo.name() + '-' + #marcaId + '-' + #modeloId + '-' + #anoId", sync = true)
    public FipeValorDTO consultarValor(TipoVeiculo tipo, String marcaId, String modeloId, String anoId) {
        try {
            FipeVeiculoResponse response = fipeClient.buscarValor(tipo.getFipePath(), marcaId, modeloId, anoId);
            if (response == null) {
                throw new RecursoNaoEncontradoException("Dados do veículo não encontrados na tabela FIPE.");
            }
            return new FipeValorDTO(
                    response.brand(),
                    response.model(),
                    response.modelYear(),
                    response.fuel(),
                    response.fuelAcronym(),
                    response.codeFipe(),
                    converterPreco(response.price()),
                    response.referenceMonth()
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("Veículo ou ano não encontrado na tabela FIPE.");
        } catch (RestClientException ex) {
            throw new FipeIndisponivelException("Não foi possível consultar o valor na tabela FIPE.", ex);
        }
    }

    public static BigDecimal converterPreco(String preco) {
        if (preco == null || preco.isBlank()) {
            return null;
        }
        String limpo = preco
                .replace("R$", "")
                .replace(".", "")
                .replace(",", ".")
                .replaceAll("\\s+", "");
        return new BigDecimal(limpo);
    }

    private List<FipeItemDTO> mapearItens(List<FipeItemResponse> itens) {
        if (itens == null) {
            return List.of();
        }
        return itens.stream()
                .map(item -> new FipeItemDTO(item.code(), item.name()))
                .toList();
    }
}
