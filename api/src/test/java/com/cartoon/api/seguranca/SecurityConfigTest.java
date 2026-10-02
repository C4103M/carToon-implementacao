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

    // --- GET /orgem-servico/** ---

    @Test
    @DisplayName("GET /orgem-servico/** - Não autenticado deve ser negado (403)")
    void getOrgemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(get("/orgem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /orgem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void getOrgemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico/1", "GET");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    @DisplayName("GET /orgem-servico/** - Usuario MECANICO deve ter acesso permitido")
    void getOrgemServico_Mecanico_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico/1", "GET");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("GET /orgem-servico/** - Usuario SUPERADMIN deve ter acesso permitido")
    void getOrgemServico_SuperAdmin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico/1", "GET");
    }

    // --- POST /orgem-servico/** ---

    @Test
    @DisplayName("POST /orgem-servico/** - Não autenticado deve ser negado (403)")
    void postOrgemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(post("/orgem-servico"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /orgem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void postOrgemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico", "POST");
    }

    @Test
    @WithMockUser(roles = "MECANICO")
    @DisplayName("POST /orgem-servico/** - Usuario MECANICO deve ter acesso permitido")
    void postOrgemServico_Mecanico_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico", "POST");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("POST /orgem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void postOrgemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(post("/orgem-servico"))
                .andExpect(status().isForbidden());
    }

    // --- PUT /orgem-servico/** ---

    @Test
    @DisplayName("PUT /orgem-servico/** - Não autenticado deve ser negado (403)")
    void putOrgemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(put("/orgem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /orgem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void putOrgemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico/1", "PUT");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("PUT /orgem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void putOrgemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(put("/orgem-servico/1"))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /orgem-servico/** ---

    @Test
    @DisplayName("DELETE /orgem-servico/** - Não autenticado deve ser negado (403)")
    void deleteOrgemServico_NaoAutenticado_DeveNegarAcesso() throws Exception {
        mockMvc.perform(delete("/orgem-servico/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /orgem-servico/** - Usuario ADMIN deve ter acesso permitido")
    void deleteOrgemServico_Admin_DevePermitirAcesso() throws Exception {
        assertAccessAllowed("/orgem-servico/1", "DELETE");
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    @DisplayName("DELETE /orgem-servico/** - Usuario SUPERADMIN deve ter acesso negado (403)")
    void deleteOrgemServico_SuperAdmin_DeveNegarAcesso() throws Exception {
        mockMvc.perform(delete("/orgem-servico/1"))
                .andExpect(status().isForbidden());
    }
}
