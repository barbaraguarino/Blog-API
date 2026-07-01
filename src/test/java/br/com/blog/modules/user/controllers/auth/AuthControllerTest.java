package br.com.blog.modules.user.controllers.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.exceptions.infrastructure.ExternalProviderAuthException;
import br.com.blog.core.security.config.SecurityConfig;
import br.com.blog.core.security.jwt.SecurityFilter;
import br.com.blog.core.security.jwt.TokenService;
import br.com.blog.core.security.userdetails.CustomUserDetailsService;
import br.com.blog.modules.user.application.dtos.auth.internal.AuthResultDTO;
import br.com.blog.modules.user.application.dtos.auth.GoogleAuthRequestDTO;
import br.com.blog.modules.user.application.dtos.auth.LoginRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.usecases.auth.AuthenticateGoogleUserService;
import br.com.blog.modules.user.application.usecases.auth.AuthenticateLocalUserService;
import br.com.blog.modules.user.presentation.controllers.auth.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
@Import({SecurityConfig.class, SecurityFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateGoogleUserService authenticateGoogleUserService;

    @MockitoBean
    private AuthenticateLocalUserService authenticateLocalUserService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserProfileResponseDTO savedUserProfileResponseDTO;

    @BeforeEach
    void setUp() {
        savedUserProfileResponseDTO = new UserProfileResponseDTO(
                UUID.randomUUID(),
                "Bárbara Nascimento",
                "barbara@blog.com",
                "barbara_1234",
                "READER",
                false,
                false,
                false,
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Login {

        private LoginRequestDTO validLoginRequestDTO;
        private AuthResultDTO authResultDTO;

        @BeforeEach
        void setUp() {
            validLoginRequestDTO = new LoginRequestDTO("barbara@blog.com", "SenhaForte@123");

            authResultDTO = new AuthResultDTO("token.jwt.gerado", savedUserProfileResponseDTO);
        }

        @Test
        @DisplayName("Deve retornar 200 OK, setar o Cookie HttpOnly e devolver o perfil do usuário")
        void shouldReturn200AndSetCookieWhenLoginIsSuccessful() throws Exception {
            when(authenticateLocalUserService.execute(any(LoginRequestDTO.class))).thenReturn(authResultDTO);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("blog_token"))
                    .andExpect(cookie().value("blog_token", "token.jwt.gerado"))
                    .andExpect(cookie().httpOnly("blog_token", true))
                    .andExpect(jsonPath("$.email").value("barbara@blog.com"))
                    .andExpect(jsonPath("$.nickname").value("barbara_1234"));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request ao enviar DTO inválido (validação @Valid)")
        void shouldReturn400WhenPayloadIsInvalid() throws Exception {
            LoginRequestDTO invalidLoginRequestDTO = new LoginRequestDTO("email-invalido", "");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLoginRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao enviar credenciais incorretas")
        void shouldReturn401WhenCredentialsAreIncorrect() throws Exception {
            when(authenticateLocalUserService.execute(any(LoginRequestDTO.class)))
                    .thenThrow(new BadCredentialsException("error.auth.bad_credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request ao enviar requisição sem corpo")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login com conta Google")
    class LoginWithGoogle {

        private GoogleAuthRequestDTO validGoogleRequest;
        private AuthResultDTO authResultDTO;

        @BeforeEach
        void setUp() {
            validGoogleRequest = new GoogleAuthRequestDTO("token.valido.do.google");

            UserProfileResponseDTO googleProfileResponse = new UserProfileResponseDTO(
                    UUID.randomUUID(), "Bárbara Google", "google@blog.com",
                    "barbara_google", "READER",
                    true, false, false, LocalDateTime.now()
            );

            authResultDTO = new AuthResultDTO("token.jwt.google.gerado", googleProfileResponse);
        }

        @Test
        @DisplayName("Deve retornar 200 OK e setar Cookie ao logar com sucesso via Google")
        void shouldReturn200AndSetCookieWhenGoogleLoginIsSuccessful() throws Exception {
            when(authenticateGoogleUserService.execute(any(GoogleAuthRequestDTO.class))).thenReturn(authResultDTO);

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("blog_token"))
                    .andExpect(cookie().value("blog_token", "token.jwt.google.gerado"))
                    .andExpect(cookie().httpOnly("blog_token", true))
                    .andExpect(jsonPath("$.isLinkedToGoogle").value(true));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found se usuário Google não estiver cadastrado no banco")
        void shouldReturn404WhenGoogleUserIsNotRegistered() throws Exception {
            when(authenticateGoogleUserService.execute(any(GoogleAuthRequestDTO.class)))
                    .thenThrow(new ResourceNotFoundException("error.auth.user_not_found_google"));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized se token Google for forjado ou inválido")
        void shouldReturn401WhenGoogleTokenIsInvalid() throws Exception {
            when(authenticateGoogleUserService.execute(any(GoogleAuthRequestDTO.class)))
                    .thenThrow(new ExternalProviderAuthException("error.auth.google_token_invalid"));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request se DTO do Google falhar na validação")
        void shouldReturn400WhenGooglePayloadIsInvalid() throws Exception {
            GoogleAuthRequestDTO emptyTokenRequest = new GoogleAuthRequestDTO("");

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyTokenRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }
    }
}