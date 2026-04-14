package br.com.grifo.core.security;

import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("Deve retornar 403 FORBIDDEN ao acessar rota protegida sem cookie")
    void shouldBlockAccessToProtectedRouteWithoutToken() throws Exception {

        mockMvc.perform(get("/api/v1/genres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve bloquear o acesso e retornar HTTP 403 ao enviar um Cookie com Token JWT forjado")
    void shouldBlockAccessWithForgedToken() throws Exception {

        String forgedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.token.falso";

        mockMvc.perform(get("/api/v1/users/perfil")
                        .cookie(new Cookie("grifo_token", forgedToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve permitir acesso à rota protegida com cookie válido contendo token JWT")
    void shouldAllowAccessToProtectedRouteWithValidToken() throws Exception {

        String validToken = jwtTokenProvider.generateToken("testuser@grifo.com");
        Cookie authCookie = new Cookie("grifo_token", validToken);

        User mockUser = new User();
        mockUser.setEmail("testuser@grifo.com");
        mockUser.setRole(UserRole.ADMIN);

        when(userRepository.findByEmail("testuser@grifo.com")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/v1/genres")
                        .cookie(authCookie))
                .andExpect(status().isMethodNotAllowed());
    }

}