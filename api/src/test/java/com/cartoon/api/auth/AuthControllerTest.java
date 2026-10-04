package com.cartoon.api.auth;

import com.cartoon.api.compartilhado.exceptions.CredenciaisInvalidasException;
import com.cartoon.api.compartilhado.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 200 OK e configurar o cookie JWT quando as credenciais forem válidas")
    void login_ComCredenciaisValidas_DeveRetornar200ECookie() throws Exception {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senha123");
        String tokenSimulado = "jwt.token.mock";

        when(authService.login(any(LoginDTO.class))).thenReturn(tokenSimulado);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=" + tokenSimulado)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=3600")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve lançar exceção ao informar credenciais inválidas")
    void login_ComCredenciaisInvalidas_DeveLancarExcecao() {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senhaIncorreta");

        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new CredenciaisInvalidasException("Email ou senha incorreto"));

        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
        );
    }

    @Test
    @DisplayName("POST /auth/logout - Deve retornar 200 OK e limpar o cookie JWT")
    void logout_DeveRetornar200ELimparCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
