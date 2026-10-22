package com.cartoon.api.cliente;

import com.cartoon.api.cliente.dto.mapper.ClienteMapper;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Deve salvar cliente com sucesso quando CPF não existir")
    void salvar_ComSucesso() {
        ClienteRequest request = new ClienteRequest();
        request.setCpf("12345678900");

        Cliente cliente = new Cliente();
        ClienteResponse responseEsperada = new ClienteResponse();

        when(clienteRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(clienteMapper.toEntity(request)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(clienteMapper.toResponse(cliente)).thenReturn(responseEsperada);

        ClienteResponse response = clienteService.salvar(request);

        assertNotNull(response);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Deve lançar ConflitoException quando CPF já existir ao salvar")
    void salvar_ComCpfExistente_LancaExcecao() {
        ClienteRequest request = new ClienteRequest();
        request.setCpf("12345678900");

        when(clienteRepository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThrows(ConflitoException.class, () -> clienteService.salvar(request));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar apenas clientes ativos")
    void listarTodos_RetornaSomenteAtivos() {
        Cliente cliente = new Cliente();
        ClienteResponse resposta = new ClienteResponse();

        when(clienteRepository.findAllByAtivoTrue()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(resposta);

        List<ClienteResponse> resultado = clienteService.listarTodos();

        assertEquals(1, resultado.size());
        verify(clienteRepository).findAllByAtivoTrue();
        verify(clienteRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve inativar o cliente sem apagá-lo do banco")
    void inativar_ComSucesso() {
        Integer id = 1;
        Cliente cliente = new Cliente();
        cliente.setId(id);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.inativar(id);

        assertFalse(cliente.isAtivo());
        verify(clienteRepository).save(cliente);
        verify(clienteRepository, never()).delete(any());
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao inativar cliente com ID inexistente")
    void inativar_ComIdInexistente_LancaExcecao() {
        Integer id = 99;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.inativar(id));

        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve reativar um cliente inativo")
    void ativar_ComSucesso() {
        Integer id = 1;
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setAtivo(false);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.ativar(id);

        assertTrue(cliente.isAtivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao reativar cliente com ID inexistente")
    void ativar_ComIdInexistente_LancaExcecao() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.ativar(99));

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir editar cliente inativo")
    void atualizar_ComClienteInativo_LancaExcecao() {
        Integer id = 1;
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setAtivo(false);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        assertThrows(ClienteInativoException.class,
                () -> clienteService.atualizar(id, new ClienteRequest()));

        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(clienteMapper);
    }
}