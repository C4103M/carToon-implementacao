package com.cartoon.api.cliente.service;

import com.cartoon.api.auth.UsuarioAutenticado;
import com.cartoon.api.cliente.dto.mapper.ClienteMapper;
import com.cartoon.api.cliente.dto.request.ClienteRequest;
import com.cartoon.api.cliente.dto.response.ClienteResponse;
import com.cartoon.api.cliente.model.Cliente;
import com.cartoon.api.cliente.repository.ClienteRepository;
import com.cartoon.api.compartilhado.exceptions.ClienteInativoException;
import com.cartoon.api.compartilhado.exceptions.ConflitoException;
import com.cartoon.api.compartilhado.exceptions.RecursoNaoEncontradoException;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.ordemServico.models.StatusServico;
import com.cartoon.api.ordemServico.repository.OrdemServicoRepository;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClienteService {
    private static final List<StatusServico> STATUS_EM_ABERTO = List.of(
            StatusServico.PENDENTE, StatusServico.ACEITO, StatusServico.EM_ANDAMENTO
    );

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final UsuarioService usuarioService;
    private final OrdemServicoRepository ordemServicoRepository;

    public Cliente buscarEntidade(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }

    public ClienteResponse buscarPorId(Integer id, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarEntidade(id);
        validarAcessoOficina(cliente, solicitante);
        return clienteMapper.toResponse(cliente);
    }

    public List<ClienteResponse> listarTodos(UsuarioAutenticado solicitante) {
        List<Cliente> clientes;
        if (solicitante == null || solicitante.isSuperAdmin()) {
            clientes = clienteRepository.findAllByAtivoTrue();
        } else {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            clientes = clienteRepository.findAllByOficinaIdAndAtivoTrue(oficina.getId());
        }
        return clientes.stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ClienteResponse> listarInativos(UsuarioAutenticado solicitante) {
        List<Cliente> clientes;
        if (solicitante == null || solicitante.isSuperAdmin()) {
            clientes = clienteRepository.findAllByAtivoFalse();
        } else {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            clientes = clienteRepository.findAllByOficinaIdAndAtivoFalse(oficina.getId());
        }
        return clientes.stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ClienteResponse> listar(Boolean ativo, UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.isSuperAdmin()) {
            if (ativo == null) {
                return clienteRepository.findAll().stream()
                        .map(clienteMapper::toResponse)
                        .collect(Collectors.toList());
            }
            return (ativo ? clienteRepository.findAllByAtivoTrue() : clienteRepository.findAllByAtivoFalse()).stream()
                    .map(clienteMapper::toResponse)
                    .collect(Collectors.toList());
        }

        Oficina oficina = obterOficinaDoSolicitante(solicitante);
        Integer oficinaId = oficina.getId();
        List<Cliente> clientes;
        if (ativo == null) {
            clientes = clienteRepository.findAllByOficinaId(oficinaId);
        } else if (ativo) {
            clientes = clienteRepository.findAllByOficinaIdAndAtivoTrue(oficinaId);
        } else {
            clientes = clienteRepository.findAllByOficinaIdAndAtivoFalse(oficinaId);
        }

        return clientes.stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse salvar(ClienteRequest request, UsuarioAutenticado solicitante) {
        Integer oficinaId = null;
        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            oficinaId = oficina.getId();
            request.setOficinaId(oficinaId);
        } else if (request.getOficinaId() != null) {
            oficinaId = request.getOficinaId();
        } else if (solicitante != null && solicitante.id() != null) {
            try {
                Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
                if (usuario != null && usuario.getOficina() != null) {
                    oficinaId = usuario.getOficina().getId();
                    request.setOficinaId(oficinaId);
                }
            } catch (Exception ignored) {}
        }

        if (oficinaId != null) {
            if (clienteRepository.existsByOficinaIdAndCpf(oficinaId, request.getCpf())) {
                throw new ConflitoException("Cliente já cadastrado com este CPF.");
            }
        } else {
            if (clienteRepository.existsByCpf(request.getCpf())) {
                throw new ConflitoException("Cliente já cadastrado com este CPF.");
            }
        }

        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(cliente);
    }

    public ClienteResponse atualizar(Integer id, ClienteRequest request, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarEntidade(id);
        validarAcessoOficina(cliente, solicitante);

        if (!cliente.isAtivo()) {
            throw new ClienteInativoException(id);
        }

        Integer oficinaId = cliente.getOficina() != null ? cliente.getOficina().getId() : null;
        if (!cliente.getCpf().equals(request.getCpf())) {
            if (oficinaId != null) {
                if (clienteRepository.existsByOficinaIdAndCpfAndIdNot(oficinaId, request.getCpf(), id)) {
                    throw new ConflitoException("Cliente já cadastrado com este CPF.");
                }
            } else if (clienteRepository.existsByCpf(request.getCpf())) {
                throw new ConflitoException("Cliente já cadastrado com este CPF.");
            }
        }

        if (solicitante != null && !solicitante.isSuperAdmin()) {
            Oficina oficina = obterOficinaDoSolicitante(solicitante);
            request.setOficinaId(oficina.getId());
        } else if (request.getOficinaId() == null) {
            if (cliente.getOficina() != null) {
                request.setOficinaId(cliente.getOficina().getId());
            } else if (solicitante != null && solicitante.id() != null) {
                Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
                if (usuario.getOficina() != null) {
                    request.setOficinaId(usuario.getOficina().getId());
                }
            }
        }

        clienteMapper.updateEntity(cliente, request);
        cliente = clienteRepository.save(cliente);
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public void inativar(Integer id, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarEntidade(id);
        validarAcessoOficina(cliente, solicitante);

        if (ordemServicoRepository != null && ordemServicoRepository.existsByVeiculoClienteIdAndStatusServicoIn(id, STATUS_EM_ABERTO)) {
            throw new ConflitoException("Não é possível inativar: o cliente possui ordens de serviço pendentes ou em andamento.");
        }

        cliente.setAtivo(false);
        if (cliente.getVeiculos() != null) {
            cliente.getVeiculos().forEach(v -> v.setAtivo(false));
        }
        clienteRepository.save(cliente);
    }

    @Transactional
    public void ativar(Integer id, UsuarioAutenticado solicitante) {
        Cliente cliente = buscarEntidade(id);
        validarAcessoOficina(cliente, solicitante);
        cliente.setAtivo(true);
        clienteRepository.save(cliente);
    }

    private Oficina obterOficinaDoSolicitante(UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.id() == null) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório.");
        }
        Usuario usuario = usuarioService.buscarEntidade(solicitante.id().intValue());
        Oficina oficina = usuario.getOficina();
        if (oficina == null) {
            throw new AccessDeniedException("Seu usuário não está vinculado a uma oficina.");
        }
        return oficina;
    }

    private void validarAcessoOficina(Cliente cliente, UsuarioAutenticado solicitante) {
        if (solicitante == null || solicitante.isSuperAdmin()) {
            return;
        }
        Oficina oficinaSolicitante = obterOficinaDoSolicitante(solicitante);
        if (cliente.getOficina() != null && !cliente.getOficina().getId().equals(oficinaSolicitante.getId())) {
            throw new AccessDeniedException("Você não tem permissão para acessar ou alterar clientes de outra oficina.");
        }
    }
}
