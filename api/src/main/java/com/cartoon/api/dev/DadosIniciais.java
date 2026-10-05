package com.cartoon.api.dev;

import com.cartoon.api.cliente.Cliente;
import com.cartoon.api.cliente.ClienteRepository;
import com.cartoon.api.oficina.model.Oficina;
import com.cartoon.api.oficina.model.OficinaRepository;
import com.cartoon.api.usuario.Role;
import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioRepository;
import com.cartoon.api.veiculo.Veiculo;
import com.cartoon.api.veiculo.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Oficina matriz = oficinaRepository.findAll().stream().findFirst().orElseGet(() -> {
            Oficina o = new Oficina();
            o.setNome("Oficina Matriz");
            o.setEndereco("Rua Exemplo, 100");
            o.setTelefone("11999990000");
            o.setAtivo(true);
            return oficinaRepository.save(o);
        });

        if (clienteRepository.count() == 0) {
            Cliente cliente = new Cliente();
            cliente.setNome("Cliente de Teste");
            cliente.setCpf("12345678901");
            cliente.setOficina(matriz);
            clienteRepository.save(cliente);

            Veiculo veiculo = new Veiculo();
            veiculo.setPlaca("ABC1D23");
            veiculo.setModelo("Gol 1.0");
            veiculo.setAno(2020);
            veiculo.setMontadora("Volkswagen");
            veiculo.setCliente(cliente);
            veiculoRepository.save(veiculo);
        }

        criarOuAtualizarUsuario("Administrador", "admin@teste.com", "senha123", Role.ADMIN, matriz);
        criarOuAtualizarUsuario("Mecânico de Teste", "mecanico@teste.com", "senha123", Role.MECANICO, matriz);
        criarOuAtualizarUsuario("Super Administrador", "superadmin@teste.com", "senha123", Role.SUPERADMIN, matriz);
    }

    private void criarOuAtualizarUsuario(String nome, String email, String senha, Role role, Oficina oficina) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(Usuario::new);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setRole(role);
        usuario.setOficina(oficina);
        usuarioRepository.save(usuario);
    }
}