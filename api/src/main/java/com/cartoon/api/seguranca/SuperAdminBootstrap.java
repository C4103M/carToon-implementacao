package com.cartoon.api.seguranca;

import com.cartoon.api.usuario.model.Role;
import com.cartoon.api.usuario.model.Usuario;
import com.cartoon.api.usuario.repository.UsuarioRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante a criacao do primeiro usuario SUPERADMIN em producao quando nao existe
 * nenhum no banco, utilizando credenciais fornecidas via variaveis de ambiente.
 */
@Log4j2
@Component
@Profile("prod")
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String email;
    private final String password;

    public SuperAdminBootstrap(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.superadmin.nome:Super Administrador}") String nome,
            @Value("${app.bootstrap.superadmin.email:}") String email,
            @Value("${app.bootstrap.superadmin.password:}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return;
        }

        if (usuarioRepository.existsByRole(Role.SUPERADMIN)) {
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.findByEmailIgnoreCase(emailNormalizado).isPresent()) {
            log.warn("Bootstrap SUPERADMIN ignorado: ja existe um usuario cadastrado com o e-mail configurado.");
            return;
        }

        String nomeFinal = (nome == null || nome.isBlank()) ? "Super Administrador" : nome.trim();

        Usuario superAdmin = new Usuario();
        superAdmin.setNome(nomeFinal);
        superAdmin.setEmail(emailNormalizado);
        superAdmin.setSenha(passwordEncoder.encode(password));
        superAdmin.setRole(Role.SUPERADMIN);
        superAdmin.setOficina(null);
        superAdmin.setAtivo(true);

        usuarioRepository.save(superAdmin);
        log.info("Primeiro usuario SUPERADMIN provisionado via bootstrap para {}.", emailNormalizado);
    }
}
