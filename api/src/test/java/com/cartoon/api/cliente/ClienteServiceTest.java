package com.cartoon.api.cliente;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.dto.mapper.ClienteMapper;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.cliente.service.ClienteService;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private final UsuarioAutenticado superAdmin = new UsuarioAutenticado(1L, "super@teste.com", "ROLE_SUPERADMIN");

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

        ClienteResponse response = clienteService.salvar(request, superAdmin);

        assertNotNull(response);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Deve lançar ConflitoException quando CPF já existir ao salvar")
    void salvar_ComCpfExistente_LancaExcecao() {
        ClienteRequest request = new ClienteRequest();
        request.setCpf("12345678900");

        when(clienteRepository.existsByCpf(request.getCpf())).thenReturn(true);

        assertThrows(ConflitoException.class, () -> clienteService.salvar(request, superAdmin));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar apenas clientes ativos")
    void listarTodos_RetornaSomenteAtivos() {
        Cliente cliente = new Cliente();
        ClienteResponse resposta = new ClienteResponse();

        when(clienteRepository.findAllByAtivoTrue()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(resposta);

        List<ClienteResponse> resultado = clienteService.listarTodos(superAdmin);

        assertEquals(1, resultado.size());
        verify(clienteRepository).findAllByAtivoTrue();
        verify(clienteRepository, never()).findAll();
    }

    @Test
    @DisplayName("Deve listar clientes da oficina do solicitante autenticado")
    void listarTodos_ComSolicitante_RetornaApenasDaOficina() {
        UsuarioAutenticado solicitante = new UsuarioAutenticado(1L, "admin@oficina.com", "ADMIN");
        Oficina oficina = new Oficina();
        oficina.setId(10);
        Usuario usuario = new Usuario();
        usuario.setOficina(oficina);

        Cliente cliente = new Cliente();
        cliente.setOficina(oficina);
        ClienteResponse resposta = new ClienteResponse();

        when(usuarioService.buscarEntidade(1)).thenReturn(usuario);
        when(clienteRepository.findAllByOficinaIdAndAtivoTrue(10)).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(resposta);

        List<ClienteResponse> resultado = clienteService.listarTodos(solicitante);

        assertEquals(1, resultado.size());
        verify(clienteRepository).findAllByOficinaIdAndAtivoTrue(10);
        verify(clienteRepository, never()).findAllByAtivoTrue();
    }

    @Test
    @DisplayName("Deve listar apenas clientes inativos")
    void listarInativos_RetornaSomenteInativos() {
        Cliente cliente = new Cliente();
        cliente.setAtivo(false);
        ClienteResponse resposta = new ClienteResponse();

        when(clienteRepository.findAllByAtivoFalse()).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(resposta);

        List<ClienteResponse> resultado = clienteService.listarInativos(superAdmin);

        assertEquals(1, resultado.size());
        verify(clienteRepository).findAllByAtivoFalse();
    }

    @Test
    @DisplayName("Deve listar clientes inativos da oficina do solicitante autenticado")
    void listarInativos_ComSolicitante_RetornaApenasInativosDaOficina() {
        UsuarioAutenticado solicitante = new UsuarioAutenticado(2L, "mecanico@oficina.com", "MECANICO");
        Oficina oficina = new Oficina();
        oficina.setId(10);
        Usuario usuario = new Usuario();
        usuario.setOficina(oficina);

        Cliente cliente = new Cliente();
        cliente.setAtivo(false);
        cliente.setOficina(oficina);
        ClienteResponse resposta = new ClienteResponse();

        when(usuarioService.buscarEntidade(2)).thenReturn(usuario);
        when(clienteRepository.findAllByOficinaIdAndAtivoFalse(10)).thenReturn(List.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(resposta);

        List<ClienteResponse> resultado = clienteService.listarInativos(solicitante);

        assertEquals(1, resultado.size());
        verify(clienteRepository).findAllByOficinaIdAndAtivoFalse(10);
        verify(clienteRepository, never()).findAllByAtivoFalse();
    }

    @Test
    @DisplayName("Deve bloquear acesso a cliente de outra oficina")
    void buscarPorId_OutraOficina_BloqueiaAcesso() {
        UsuarioAutenticado solicitante = new UsuarioAutenticado(1L, "admin@oficina1.com", "ADMIN");
        Oficina oficinaSolicitante = new Oficina();
        oficinaSolicitante.setId(1);
        Usuario usuario = new Usuario();
        usuario.setOficina(oficinaSolicitante);

        Oficina oficinaOutra = new Oficina();
        oficinaOutra.setId(2);
        Cliente cliente = new Cliente();
        cliente.setId(100);
        cliente.setOficina(oficinaOutra);

        when(clienteRepository.findById(100)).thenReturn(Optional.of(cliente));
        when(usuarioService.buscarEntidade(1)).thenReturn(usuario);

        assertThrows(AccessDeniedException.class, () -> clienteService.buscarPorId(100, solicitante));
    }

    @Test
    @DisplayName("Deve inativar o cliente sem apagá-lo do banco")
    void inativar_ComSucesso() {
        Integer id = 1;
        Cliente cliente = new Cliente();
        cliente.setId(id);

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.inativar(id, superAdmin);

        assertFalse(cliente.isAtivo());
        verify(clienteRepository).save(cliente);
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao inativar cliente com ID inexistente")
    void inativar_ComIdInexistente_LancaExcecao() {
        Integer id = 99;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.inativar(id, superAdmin));

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

        clienteService.ativar(id, superAdmin);

        assertTrue(cliente.isAtivo());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Deve lançar exceção ao reativar cliente com ID inexistente")
    void ativar_ComIdInexistente_LancaExcecao() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> clienteService.ativar(99, superAdmin));

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
                () -> clienteService.atualizar(id, new ClienteRequest(), superAdmin));

        verify(clienteRepository, never()).save(any());
        verifyNoInteractions(clienteMapper);
    }
}
