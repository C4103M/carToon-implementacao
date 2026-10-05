package com.cartoon.api.cliente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteRepository;
import com.cartoon.api.cliente.ClienteService;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNome("Carlos Eduardo Silva");
        cliente.setCpf("12345678900");
    }

    @Test
    void deveSalvarNovoClienteComSucesso() {
        when(clienteRepository.existsByCpf(cliente.getCpf())).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.salvar(cliente);

        assertNotNull(resultado);
        verify(clienteRepository).existsByCpf(cliente.getCpf());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void deveLancarExcecaoQuandoCpfDuplicadoNaInclusao() {
        when(clienteRepository.existsByCpf(cliente.getCpf())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.salvar(cliente);
        });

        assertEquals("Cliente já cadastrado no sistema.", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }
}