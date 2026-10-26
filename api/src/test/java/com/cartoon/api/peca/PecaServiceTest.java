package com.cartoon.api.peca;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.peca.dto.PecaFiltro;
import com.cartoon.api.peca.dto.request.PecaRequest;
import com.cartoon.api.peca.dto.response.PecaResponse;
import com.cartoon.api.peca.models.Peca;
import com.cartoon.api.peca.repositories.PecaRepository;
import com.cartoon.api.peca.service.PecaService;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PecaService pecaService;

    @Test
    @DisplayName("Deve salvar uma peça com sucesso")
    void deveSalvarPecaComSucesso() {
        PecaRequest request = new PecaRequest("Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        Peca pecaSalva = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));

        when(pecaRepository.save(any(Peca.class))).thenReturn(pecaSalva);

        PecaResponse response = pecaService.salvar(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Filtro de Óleo", response.nome());
        verify(pecaRepository, times(1)).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve salvar uma peça associando à oficina do solicitante")
    void deveSalvarPecaAssociandoOficina() {
        Oficina oficina = new Oficina();
        oficina.setId(10);

        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setOficina(oficina);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(5L, "admin@oficina.com", "ADMIN");
        PecaRequest request = new PecaRequest("Filtro de Óleo", "Bosch", new BigDecimal("50.00"));

        when(usuarioService.buscarEntidade(5)).thenReturn(usuario);
        when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> {
            Peca p = inv.getArgument(0);
            p.setId(1);
            return p;
        });

        PecaResponse response = pecaService.salvar(request, solicitante);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(10, response.oficinaId());
    }

    @Test
    @DisplayName("Deve bloquear acesso a peça de outra oficina")
    void deveBloquearAcessoPecaOutraOficina() {
        Oficina oficina1 = new Oficina();
        oficina1.setId(10);

        Oficina oficina2 = new Oficina();
        oficina2.setId(20);

        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setOficina(oficina1);

        Peca pecaOutraOficina = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        pecaOutraOficina.setOficina(oficina2);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(5L, "admin@oficina.com", "ADMIN");

        when(pecaRepository.findById(1)).thenReturn(Optional.of(pecaOutraOficina));
        when(usuarioService.buscarEntidade(5)).thenReturn(usuario);

        assertThrows(AccessDeniedException.class, () -> pecaService.buscar(1, solicitante));
    }

    @Test
    @DisplayName("Deve permitir acesso de SUPERADMIN (ROLE_SUPERADMIN) a peça mesmo sem oficina")
    void devePermitirAcessoSuperAdminMesmoSemOficina() {
        Oficina oficina = new Oficina();
        oficina.setId(20);

        Peca peca = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        peca.setOficina(oficina);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");

        when(pecaRepository.findById(1)).thenReturn(Optional.of(peca));

        PecaResponse response = pecaService.buscar(1, solicitante);

        assertNotNull(response);
        assertEquals(1, response.id());
    }

    @Test
    @DisplayName("Deve buscar uma peça por ID com sucesso")
    void deveBuscarPecaPorIdComSucesso() {
        Peca peca = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        when(pecaRepository.findById(1)).thenReturn(Optional.of(peca));

        PecaResponse response = pecaService.buscar(1);

        assertNotNull(response);
        assertEquals(1, response.id());
        verify(pecaRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar peça inexistente")
    void deveLancarExcecaoAoBuscarPecaInexistente() {
        when(pecaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> pecaService.buscar(99));
        verify(pecaRepository, times(1)).findById(99);
    }

    @Test
    @DisplayName("Deve listar peças paginadas com sucesso")
    void deveListarPecasPaginadas() {
        Peca peca = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        Page<Peca> pagina = new PageImpl<>(List.of(peca));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(pecaRepository.findAll(any(Specification.class), eq(pageRequest))).thenReturn(pagina);

        Page<PecaResponse> resultado = pecaService.listar(pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(pecaRepository, times(1)).findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("Deve listar peças filtradas por termo (nome ou fabricante) com sucesso")
    void deveListarPecasFiltradasPorBusca() {
        Peca peca = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        Page<Peca> pagina = new PageImpl<>(List.of(peca));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(pecaRepository.findAll(any(Specification.class), eq(pageRequest)))
                .thenReturn(pagina);

        Page<PecaResponse> resultado = pecaService.listar(new PecaFiltro("Bosch", null, null), pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(pecaRepository, times(1))
                .findAll(any(Specification.class), eq(pageRequest));
    }

    @Test
    @DisplayName("Deve atualizar uma peça com sucesso")
    void deveAtualizarPecaComSucesso() {
        Peca pecaExistente = new Peca(1, "Filtro", "MarcaA", new BigDecimal("20.00"));
        PecaRequest requestAtualizacao = new PecaRequest("Filtro de Ar", "Bosch", new BigDecimal("30.00"));
        
        when(pecaRepository.findById(1)).thenReturn(Optional.of(pecaExistente));
        when(pecaRepository.save(any(Peca.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PecaResponse response = pecaService.atualizar(1, requestAtualizacao);

        assertNotNull(response);
        assertEquals("Filtro de Ar", response.nome());
        assertEquals("Bosch", response.fabricante());
        assertEquals(new BigDecimal("30.00"), response.valorBase());
        verify(pecaRepository, times(1)).findById(1);
        verify(pecaRepository, times(1)).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve deletar uma peça com sucesso (soft delete)")
    void deveDeletarPecaComSucesso() {
        Peca peca = new Peca(1, "Filtro de Óleo", "Bosch", new BigDecimal("50.00"));
        when(pecaRepository.findById(1)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any(Peca.class))).thenReturn(peca);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");
        assertDoesNotThrow(() -> pecaService.deletar(1, solicitante));
        verify(pecaRepository, times(1)).findById(1);
        verify(pecaRepository, times(1)).save(peca);
        assertFalse(peca.getAtivo());
    }
}
