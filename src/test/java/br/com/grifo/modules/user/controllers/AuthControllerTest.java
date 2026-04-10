package br.com.grifo.modules.user.controllers;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.LoginRequestDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("Login com email e senha.")
    class SimpleLogin{

        @Test
        @DisplayName("Deve retornar 200 OK, Cookie HttpOnly e DTO do usuário ao logar com sucesso")
        void shouldReturn200AndCookieWhenLoginIsSuccessful() throws Exception {
            LoginRequestDTO requestDTO = new LoginRequestDTO("barbara@grifo.com", "SenhaForte@123");
            UserResponseDTO userDTO = new UserResponseDTO(
                    UUID.randomUUID(), "Bárbara Guarino", "barbara@grifo.com",
                    "barbara_1234", "READER",false, true, false, LocalDateTime.now()
            );
            AuthService.AuthResult authResult = new AuthService.AuthResult("token.jwt.falso", userDTO);
            when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResult);
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    // 👇 A MÁGICA AQUI: Testando se o Cookie de segurança foi criado corretamente!
                    .andExpect(cookie().exists("grifo_token"))
                    .andExpect(cookie().value("grifo_token", "token.jwt.falso"))
                    .andExpect(cookie().httpOnly("grifo_token", true))
                    .andExpect(jsonPath("$.email").value("barbara@grifo.com"))
                    .andExpect(jsonPath("$.nickname").value("barbara_1234"));
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao enviar credenciais invalidas")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            LoginRequestDTO requestDTO = new LoginRequestDTO("barbara@grifo.com", "senha-errada");
            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Login via Google")
    class LoginWithGoogle{

        @Test
        @DisplayName("Deve retornar 200 OK e Cookie HttpOnly ao logar com Google com sucesso")
        void shouldReturn200AndCookieWhenGoogleLoginIsSuccessful() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.valido.do.google");

            UserResponseDTO userDTO = new UserResponseDTO(
                    UUID.randomUUID(), "Bárbara Google", "barbara.google@grifo.com",
                    "barbara_g", "READER", true, true, false, LocalDateTime.now()
            );

            AuthService.AuthResult authResult = new AuthService.AuthResult("token.jwt.google.falso", userDTO);

            when(authService.authenticateWithGoogle(any(GoogleTokenDTO.class))).thenReturn(authResult);

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("grifo_token"))
                    .andExpect(cookie().value("grifo_token", "token.jwt.google.falso"))
                    .andExpect(cookie().httpOnly("grifo_token", true))
                    .andExpect(jsonPath("$.email").value("barbara.google@grifo.com"))
                    .andExpect(jsonPath("$.isLinkedToGoogle").value(true));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found se usuário tentar logar com Google sem estar cadastrado")
        void shouldReturn404WhenGoogleUserIsNotRegistered() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.valido.mas.sem.cadastro");

            when(authService.authenticateWithGoogle(any(GoogleTokenDTO.class)))
                    .thenThrow(new BusinessException("error.auth.user_not_found_google", HttpStatus.NOT_FOUND));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

}