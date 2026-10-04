package com.cartoon.api.seguranca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityConfigTest.TestSecurityConfig.class)
@WebAppConfiguration
class SecurityConfigTest {

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import(SecurityConfig.class)
    static class TestSecurityConfig {}

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private void assertAccessAllowed(String path, String httpMethod) throws Exception {
        var requestBuilder = switch (httpMethod.toUpperCase()) {
            case "GET" -> get(path);
            case "POST" -> post(path);
            case "PUT" -> put(path);
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        };

        mockMvc.perform(requestBuilder)
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertNotEquals(401, status, "Request should not be unauthorized (401)");
                    assertNotEquals(403, status, "Request should not be forbidden (403)");
                });
    }

    // --- /auth/** (PermitAll) ---

    @Test
    @DisplayName("Permissões /auth/** - Não autenticado deve ter acesso permitido aos endpoints de auth")
    void auth_NaoAutenticado_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/auth/login", "POST");
    }

    // --- GET /ordem-servico/** ---

    @Test
    @DisplayName("GET /ordem-servico/** - Não autenticado deve ser negado (403)")
    void getOrdemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(get("/ordem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /ordem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void getOrdemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico/1", "GET");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    @DisplayName("GET /ordem-servico/** - Usuario MECANICO deve ter acesso permitido")
    void getOrdemServico_Mecanico_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico/1", "GET");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("GET /ordem-servico/** - Usuario SUPERADMIN deve ter acesso permitido")
    void getOrdemServico_SuperAdmin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico/1", "GET");
    }

    // --- POST /ordem-servico/** ---

    @Test
    @DisplayName("POST /ordem-servico/** - Não autenticado deve ser negado (403)")
    void postOrdemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(post("/ordem-servico"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /ordem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void postOrdemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico", "POST");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    @DisplayName("POST /ordem-servico/** - Usuario MECANICO deve ter acesso permitido")
    void postOrdemServico_Mecanico_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico", "POST");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("POST /ordem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void postOrdemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(post("/ordem-servico"))
                .andExpect(status().isForbidden());
    }

    // --- PUT /ordem-servico/** ---

    @Test
    @DisplayName("PUT /ordem-servico/** - Não autenticado deve ser negado (403)")
    void putOrdemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(put("/ordem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /ordem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void putOrdemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico/1", "PUT");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("PUT /ordem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void putOrdemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(put("/ordem-servico/1"))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /ordem-servico/** ---

    @Test
    @DisplayName("DELETE /ordem-servico/** - Não autenticado deve ser negado (403)")
    void deleteOrdemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(delete("/ordem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /ordem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void deleteOrdemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/ordem-servico/1", "DELETE");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("DELETE /ordem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void deleteOrdemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(delete("/ordem-servico/1"))
                .andExpect(status().isForbidden());
    }
}
