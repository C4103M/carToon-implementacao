package com.cartoon.api.servico;

import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repositories.ServicoRepository;
import com.cartoon.api.servico.service.ServicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    @DisplayName("Deve salvar um serviço com sucesso")
    void deveSalvarServicoComSucesso() {
        ServicoRequest request = new ServicoRequest("Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        Servico servicoSalvo = new Servico(1, "Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));

        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoSalvo);

        ServicoResponse response = servicoService.salvar(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Troca de Óleo", response.nome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve buscar um serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        Servico servico = new Servico(1, "Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));

        ServicoResponse response = servicoService.buscar(1);

        assertNotNull(response);
        assertEquals(1, response.id());
        verify(servicoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar serviço inexistente")
    void deveLancarExcecaoAoBuscarServicoInexistente() {
        when(servicoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> servicoService.buscar(99));
        verify(servicoRepository, times(1)).findById(99);
    }

    @Test
    @DisplayName("Deve listar serviços paginados com sucesso")
    void deveListarServicosPaginados() {
        Servico servico = new Servico(1, "Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        Page<Servico> pagina = new PageImpl<>(List.of(servico));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(servicoRepository.findAll(pageRequest)).thenReturn(pagina);

        Page<ServicoResponse> resultado = servicoService.listar(pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(servicoRepository, times(1)).findAll(pageRequest);
    }

    @Test
    @DisplayName("Deve listar serviços filtrados por nome com sucesso")
    void deveListarServicosFiltradosPorNome() {
        Servico servico = new Servico(1, "Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        Page<Servico> pagina = new PageImpl<>(List.of(servico));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(servicoRepository.findByNomeContainingIgnoreCase("Óleo", pageRequest)).thenReturn(pagina);

        Page<ServicoResponse> resultado = servicoService.listar("Óleo", pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(servicoRepository, times(1)).findByNomeContainingIgnoreCase("Óleo", pageRequest);
    }

    @Test
    @DisplayName("Deve atualizar um serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        Servico servicoExistente = new Servico(1, "Troca", new BigDecimal("50.00"), "Rapida", LocalTime.of(0, 30));
        ServicoRequest requestAtualizacao = new ServicoRequest("Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servicoExistente));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse response = servicoService.atualizar(1, requestAtualizacao);

        assertNotNull(response);
        assertEquals("Troca de Óleo", response.nome());
        assertEquals(new BigDecimal("100.00"), response.valorBase());
        verify(servicoRepository, times(1)).findById(1);
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve deletar um serviço com sucesso")
    void deveDeletarServicoComSucesso() {
        Servico servico = new Servico(1, "Troca de Óleo", new BigDecimal("100.00"), "Troca completa", LocalTime.of(1, 0));
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        doNothing().when(servicoRepository).delete(servico);

        assertDoesNotThrow(() -> servicoService.deletar(1));
        verify(servicoRepository, times(1)).findById(1);
        verify(servicoRepository, times(1)).delete(servico);
    }
}
