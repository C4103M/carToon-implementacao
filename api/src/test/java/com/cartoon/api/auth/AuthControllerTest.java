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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, false);
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
    @DisplayName("POST /auth/login - Deve retornar 401 Unauthorized ao informar credenciais inválidas")
    void login_ComCredenciaisInvalidas_DeveLancarExcecao() throws Exception {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senhaIncorreta");

        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new CredenciaisInvalidasException("Email ou senha incorreto"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/logout - Deve retornar 200 OK e limpar o cookie JWT")
    void logout_DeveRetornar200ELimparCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("jwt=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 Bad Request quando e-mail estiver em branco ou nulo")
    void login_ComEmailEmBranco_DeveRetornar400() throws Exception {
        LoginDTO dto = new LoginDTO("", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errosValidacao.email").exists());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 Bad Request quando formato do e-mail for inválido")
    void login_ComFormatoEmailInvalido_DeveRetornar400() throws Exception {
        LoginDTO dto = new LoginDTO("email-sem-arroba", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errosValidacao.email").value("E-mail inválido"));
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 Bad Request quando senha estiver em branco ou nula")
    void login_ComSenhaEmBranco_DeveRetornar400() throws Exception {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "   ");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errosValidacao.password").exists());
    }

    @Test
    @DisplayName("POST /auth/login - Deve retornar 400 Bad Request quando conta estiver bloqueada por tentativas")
    void login_ContaBloqueada_DeveRetornar400ComMensagem() throws Exception {
        LoginDTO dto = new LoginDTO("usuario@cartoon.com", "senha123");

        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new com.cartoon.api.compartilhado.exceptions.BloqueioTentativasException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Número de tentativas excedido. Conta temporariamente bloqueada por 15 minutos."));
    }
}
