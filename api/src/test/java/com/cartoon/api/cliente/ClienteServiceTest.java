package com.cartoon.api.cliente;

import com.cartoon.api.cliente.dto.mapper.ClienteMapper;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("Deve excluir cliente com sucesso quando ID existir")
    void excluir_ComSucesso() {
        Integer id = 1;
        Cliente cliente = new Cliente();
        cliente.setId(id);

        when(clienteRepository.findById(id)).thenReturn(java.util.Optional.of(cliente));

        assertDoesNotThrow(() -> clienteService.excluir(id));

        verify(clienteRepository).findById(id);
        verify(clienteRepository).delete(cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar excluir cliente com ID inexistente")
    void excluir_ComIdInexistente_LancaExcecao() {
        Integer id = 99;

        when(clienteRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> clienteService.excluir(id));

        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).delete(any());
    }
}