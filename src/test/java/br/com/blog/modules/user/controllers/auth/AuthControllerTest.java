package br.com.blog.modules.user.controllers.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.*;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.services.auth.AuthService;
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
import org.springframework.test.util.ReflectionTestUtils;
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
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User savedUser;
    private UserProfileResponse savedUserProfileResponse;

    @BeforeEach
    void setUp() {
        savedUser = User.createLocalUser(
                "Bárbara Nascimento",
                "barbara@blog.com",
                "senha-criptografada"
        );

        ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedUser, "createdAt", LocalDateTime.now());

        savedUserProfileResponse = new UserProfileResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                "barbara_1234",
                savedUser.getRole().name(),
                savedUser.isLinkedToGoogle(),
                savedUser.isEnabled(),
                savedUser.isLocked(),
                savedUser.getCreatedAt()
        );
    }

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Login {

        private LoginRequest validLoginRequest;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            validLoginRequest = new LoginRequest("barbara@blog.com", "SenhaForte@123");
            authResult = new AuthService.AuthResult("token.jwt.gerado", savedUser);
        }

        @Test
        @DisplayName("Deve retornar 200 OK, setar o Cookie HttpOnly e devolver o perfil do usuário")
        void shouldReturn200AndSetCookieWhenLoginIsSuccessful() throws Exception {
            when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserProfileResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
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
            LoginRequest invalidLoginRequest = new LoginRequest("email-invalido", "");

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao enviar credenciais incorretas")
        void shouldReturn401WhenCredentialsAreIncorrect() throws Exception {
            when(authService.authenticate(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("error.auth.bad_credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
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

        private GoogleAuthRequest validGoogleRequest;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            validGoogleRequest = new GoogleAuthRequest("token.valido.do.google");
            ReflectionTestUtils.setField(savedUser, "googleId", "google-id-1234");

            authResult = new AuthService.AuthResult("token.jwt.google.gerado", savedUser);

            savedUserProfileResponse = new UserProfileResponse(
                    savedUser.getId(), savedUser.getName(), savedUser.getEmail(),
                    savedUser.getNickname(), savedUser.getRole().name(),
                    true, // isLinkedToGoogle
                    savedUser.isEnabled(), savedUser.isLocked(), savedUser.getCreatedAt()
            );
        }

        @Test
        @DisplayName("Deve retornar 200 OK e setar Cookie ao logar com sucesso via Google")
        void shouldReturn200AndSetCookieWhenGoogleLoginIsSuccessful() throws Exception {
            when(authService.authenticateWithGoogle(any(GoogleAuthRequest.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserProfileResponse);

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
            when(authService.authenticateWithGoogle(any(GoogleAuthRequest.class)))
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
            when(authService.authenticateWithGoogle(any(GoogleAuthRequest.class)))
                    .thenThrow(new BadCredentialsException("error.auth.google_token_invalid"));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validGoogleRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request se DTO do Google falhar na validação")
        void shouldReturn400WhenGooglePayloadIsInvalid() throws Exception {
            GoogleAuthRequest emptyTokenRequest = new GoogleAuthRequest("");

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyTokenRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }
    }
}