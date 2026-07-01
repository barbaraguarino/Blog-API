package br.com.blog.modules.user.controllers.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.core.exceptions.infrastructure.ExternalProviderAuthException;
import br.com.blog.core.security.config.SecurityConfig;
import br.com.blog.core.security.jwt.SecurityFilter;
import br.com.blog.core.security.jwt.TokenService;
import br.com.blog.core.security.userdetails.CustomUserDetailsService;
import br.com.blog.modules.user.application.dtos.auth.GoogleAuthRequestDTO;
import br.com.blog.modules.user.application.dtos.registration.RegisterUserRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.usecases.registration.RegisterGoogleUserService;
import br.com.blog.modules.user.application.usecases.registration.RegisterLocalUserService;
import br.com.blog.modules.user.presentation.controllers.registration.UserRegistrationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@Import({SecurityConfig.class, SecurityFilter.class})
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private TokenService tokenService;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @MockitoBean private RegisterLocalUserService registerLocalUserService;
    @MockitoBean private RegisterGoogleUserService registerGoogleUserService;

    private UserProfileResponseDTO mockProfileResponse;

    @BeforeEach
    void setUp() {
        mockProfileResponse = new UserProfileResponseDTO(
                UUID.randomUUID(),
                "Bárbara",
                "barbara@blog.com",
                "barbara_nickname",
                "READER",
                false,
                false,
                false,
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Cadastro com e-mail e senha")
    class Register {

        private RegisterUserRequestDTO validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new RegisterUserRequestDTO("Bárbara", "barbara@blog.com", "SenhaForte@123");
        }

        @Test
        @DisplayName("Deve retornar HTTP 201 (Created) e o perfil do usuário quando dados forem válidos")
        void shouldReturn201WhenPayloadIsValid() throws Exception {
            when(registerLocalUserService.execute(any(RegisterUserRequestDTO.class))).thenReturn(mockProfileResponse);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.email").value("barbara@blog.com"))
                    .andExpect(jsonPath("$.nickname").value("barbara_nickname"));
        }

        @Test
        @DisplayName("Deve retornar HTTP 400 (Bad Request) quando DTO não passar na validação (@Valid)")
        void shouldReturn400WhenPayloadIsInvalid() throws Exception {
            RegisterUserRequestDTO invalidRequest = new RegisterUserRequestDTO("Bárbara", "barbara@blog.com", "123");

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }

        @Test
        @DisplayName("Deve retornar HTTP 409 (Conflict) quando e-mail já existir no banco")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            when(registerLocalUserService.execute(any(RegisterUserRequestDTO.class)))
                    .thenThrow(new ResourceAlreadyExistsException("error.user.already_exists"));

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Deve retornar HTTP 400 (Bad Request) ao enviar requisição sem corpo (Body vazio)")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Cadastro com a Conta Google")
    class RegisterWithGoogle {

        private GoogleAuthRequestDTO validGoogleRequest;

        @BeforeEach
        void setUp() {
            validGoogleRequest = new GoogleAuthRequestDTO("google.token.valido");
        }

        @Test
        @DisplayName("Deve retornar HTTP 201 (Created) ao processar token válido do Google")
        void shouldReturn201WhenGoogleTokenIsValid() throws Exception {
            when(registerGoogleUserService.execute(any(GoogleAuthRequestDTO.class))).thenReturn(mockProfileResponse);

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.email").value("barbara@blog.com"));
        }

        @Test
        @DisplayName("Deve retornar HTTP 400 (Bad Request) quando token Google for vazio")
        void shouldReturn400WhenGoogleTokenIsEmpty() throws Exception {
            GoogleAuthRequestDTO invalidGoogleRequest = new GoogleAuthRequestDTO("");

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidGoogleRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }

        @Test
        @DisplayName("Deve retornar HTTP 409 (Conflict) quando conta Google já estiver vinculada a um usuário")
        void shouldReturn409WhenGoogleUserAlreadyExists() throws Exception {
            when(registerGoogleUserService.execute(any(GoogleAuthRequestDTO.class)))
                    .thenThrow(new ResourceAlreadyExistsException("error.user.already_exists"));

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Deve retornar HTTP 401 (Unauthorized) quando token do Google for inválido ou forjado")
        void shouldReturn401WhenGoogleTokenIsInvalid() throws Exception {
            when(registerGoogleUserService.execute(any(GoogleAuthRequestDTO.class)))
                    .thenThrow(new ExternalProviderAuthException("error.auth.google_token_invalid"));

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }
}