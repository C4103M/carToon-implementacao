package com.cartoon.api.dev;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteRepository;
import com.cartoon.api.oficina.Oficina;
import com.cartoon.api.oficina.OficinaRepository;
import com.cartoon.api.usuario.Role;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioRepository;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DadosIniciais implements ApplicationRunner {

    private final OficinaRepository oficinaRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (oficinaRepository.count() > 0) {
            return;                       // já populado (útil com ddl-auto=update)
        }

        Oficina matriz = new Oficina();
        matriz.setNome("Oficina Matriz");
        matriz.setEndereco("Rua Exemplo, 100");
        matriz.setTelefone("11999990000");
        matriz.setMatriz(true);
        oficinaRepository.save(matriz);

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente de Teste");
        cliente.setCpf("12345678901");
        cliente.setOficina(matriz);
        clienteRepository.save(cliente);

        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca("ABC1D23");
        veiculo.setCliente(cliente);
        veiculoRepository.save(veiculo);

        Usuario mecanico = new Usuario();
        mecanico.setNome("Mecânico de Teste");
        mecanico.setEmail("mecanico@teste.com");
        mecanico.setSenha("trocar-quando-houver-seguranca");
        mecanico.setRole(Role.MECANICO);
        mecanico.setOficina(matriz);
        usuarioRepository.save(mecanico);
    }
}