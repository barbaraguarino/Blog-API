package br.com.grifo.core.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("Deve bloquear o acesso e retornar HTTP 403 ao tentar acessar rota protegida sem token")
    void shouldBlockAccessToProtectedRouteWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/perfil"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir o acesso a rota protegida enviando um Token JWT válido")
    void shouldAllowAccessToProtectedRouteWithValidToken() throws Exception {
        String token = jwtTokenProvider.generateToken("testuser@grifo.com");
        mockMvc.perform(get("/api/v1/users/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve bloquear o acesso e retornar HTTP 403 ao enviar um Token JWT forjado")
    void shouldBlockAccessWithForgedToken() throws Exception {
        String forgedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token.falso";
        mockMvc.perform(get("/api/v1/users/perfil")
                        .header("Authorization", "Bearer " + forgedToken))
                .andExpect(status().isForbidden());
    }

}