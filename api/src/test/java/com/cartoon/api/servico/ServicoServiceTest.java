package com.cartoon.api.servico;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.servico.dto.request.ServicoRequest;
import com.cartoon.api.servico.dto.response.ServicoResponse;
import com.cartoon.api.servico.models.Servico;
import com.cartoon.api.servico.repository.ServicoRepository;
import com.cartoon.api.servico.service.ServicoService;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    @DisplayName("Deve salvar um serviço com sucesso")
    void deveSalvarServicoComSucesso() {
        ServicoRequest request = new ServicoRequest("Troca de Óleo", 1.0, "Troca completa");
        Servico servicoSalvo = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");

        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoSalvo);

        ServicoResponse response = servicoService.salvar(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals("Troca de Óleo", response.nome());
        assertEquals(1.0, response.quantidadeHoras());
        assertEquals("Troca completa", response.descricao());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve salvar um serviço associando à oficina do solicitante")
    void deveSalvarServicoAssociandoOficina() {
        Oficina oficina = new Oficina();
        oficina.setId(10);

        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setOficina(oficina);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(5L, "admin@oficina.com", "ADMIN");
        ServicoRequest request = new ServicoRequest("Alinhamento", 1.5, "Alinhamento 3D");

        when(usuarioService.buscarEntidade(5)).thenReturn(usuario);
        when(servicoRepository.save(any(Servico.class))).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            s.setId(1);
            return s;
        });

        ServicoResponse response = servicoService.salvar(request, solicitante);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(10, response.oficinaId());
        assertEquals(1.5, response.quantidadeHoras());
    }

    @Test
    @DisplayName("Deve buscar um serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        Servico servico = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));

        ServicoResponse response = servicoService.buscar(1);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(1.0, response.quantidadeHoras());
        verify(servicoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Deve bloquear acesso a serviço de outra oficina")
    void deveBloquearAcessoServicoOutraOficina() {
        Oficina oficina1 = new Oficina();
        oficina1.setId(10);

        Oficina oficina2 = new Oficina();
        oficina2.setId(20);

        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setOficina(oficina1);

        Servico servicoOutraOficina = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");
        servicoOutraOficina.setOficina(oficina2);

        UsuarioAutenticado solicitante = new UsuarioAutenticado(5L, "admin@oficina.com", "ADMIN");

        when(servicoRepository.findById(1)).thenReturn(Optional.of(servicoOutraOficina));
        when(usuarioService.buscarEntidade(5)).thenReturn(usuario);

        assertThrows(AccessDeniedException.class, () -> servicoService.buscar(1, solicitante));
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
        Servico servico = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");
        Page<Servico> pagina = new PageImpl<>(List.of(servico));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(servicoRepository.findAll(pageRequest)).thenReturn(pagina);

        Page<ServicoResponse> resultado = servicoService.listar(pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1.0, resultado.getContent().get(0).quantidadeHoras());
        verify(servicoRepository, times(1)).findAll(pageRequest);
    }

    @Test
    @DisplayName("Deve listar serviços filtrados por nome com sucesso")
    void deveListarServicosFiltradosPorNome() {
        Servico servico = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");
        Page<Servico> pagina = new PageImpl<>(List.of(servico));
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(servicoRepository.findByNomeContainingIgnoreCase("Óleo", pageRequest)).thenReturn(pagina);

        Page<ServicoResponse> resultado = servicoService.listar("Óleo", pageRequest);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Troca de Óleo", resultado.getContent().get(0).nome());
        verify(servicoRepository, times(1)).findByNomeContainingIgnoreCase("Óleo", pageRequest);
    }

    @Test
    @DisplayName("Deve atualizar um serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        Servico servicoExistente = new Servico(1, "Troca", 0.5, "Rapida");
        ServicoRequest requestAtualizacao = new ServicoRequest("Troca de Óleo", 1.5, "Troca completa");

        when(servicoRepository.findById(1)).thenReturn(Optional.of(servicoExistente));
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse response = servicoService.atualizar(1, requestAtualizacao);

        assertNotNull(response);
        assertEquals("Troca de Óleo", response.nome());
        assertEquals(1.5, response.quantidadeHoras());
        assertEquals("Troca completa", response.descricao());
        verify(servicoRepository, times(1)).findById(1);
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve deletar um serviço com sucesso")
    void deveDeletarServicoComSucesso() {
        Servico servico = new Servico(1, "Troca de Óleo", 1.0, "Troca completa");
        when(servicoRepository.findById(1)).thenReturn(Optional.of(servico));
        doNothing().when(servicoRepository).delete(servico);

        assertDoesNotThrow(() -> servicoService.deletar(1));
        verify(servicoRepository, times(1)).findById(1);
        verify(servicoRepository, times(1)).delete(servico);
    }
}
