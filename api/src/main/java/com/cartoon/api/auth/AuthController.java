package com.cartoon.api.auth;


import com.cartoon.api.usuario.Usuario;
import com.cartoon.api.usuario.UsuarioRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/login")
    public ResponseEntity<Void> login(@RequestBody LoginDTO loginDTO) {
        authService.login(loginDTO);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/cadastrar")
    public ResponseEntity<Usuario> cadastrar(@RequestBody CadastroDTO cadastroDTO) {

    }
}
