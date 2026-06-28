package br.com.blog.modules.user.controllers.auth;

import br.com.blog.core.exceptions.domain.BusinessRuleException;
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
import org.springframework.http.HttpStatus;
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
                "barbara@grifo.com",
                "senha-criptografada",
                "barbara_1234"
        );

        ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedUser, "googleId", "token.valido.do.google");
        ReflectionTestUtils.setField(savedUser, "createdAt", LocalDateTime.now());

        savedUserProfileResponse = new UserProfileResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getRole().toString(),
                savedUser.isLinkedToGoogle(),
                savedUser.isEnabled(),
                savedUser.isLocked(),
                savedUser.getCreatedAt()
        );
    }

    @Nested
    @DisplayName("Login simples, usando e-mail e senha")
    class Login {

        private LoginRequest loginRequest;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest("barbara@grifo.com", "SenhaForte@123");
            authResult = new AuthService.AuthResult("token.jwt.falso", savedUser);
        }

        @Test
        @DisplayName("Deve retornar 200 OK, Cookie HttpOnly e DTO do usuário ao logar com sucesso")
        void shouldReturn200AndCookieWhenLoginIsSuccessful() throws Exception {
            when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserProfileResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("grifo_token"))
                    .andExpect(cookie().value("grifo_token", "token.jwt.falso"))
                    .andExpect(cookie().httpOnly("grifo_token", true))
                    .andExpect(jsonPath("$.email").value("barbara@grifo.com"))
                    .andExpect(jsonPath("$.nickname").value("barbara_1234"));
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao enviar credenciais invalidas")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {

            when(authService.authenticate(any(LoginRequest.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Login com conta Google")
    class LoginWithGoogle {

        private GoogleAuthRequest googleAuthRequest;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            googleAuthRequest = new GoogleAuthRequest("token.valido.do.google");
            authResult = new AuthService.AuthResult("token.jwt.google.falso", savedUser);
        }

        @Test
        @DisplayName("Deve retornar 200 OK e Cookie HttpOnly ao logar com Google com sucesso")
        void shouldReturn200AndCookieWhenGoogleLoginIsSuccessful() throws Exception {
            when(authService.authenticateWithGoogle(any(GoogleAuthRequest.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserProfileResponse);

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleAuthRequest)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("grifo_token"))
                    .andExpect(cookie().value("grifo_token", "token.jwt.google.falso"))
                    .andExpect(cookie().httpOnly("grifo_token", true))
                    .andExpect(jsonPath("$.email").value("barbara@grifo.com"))
                    .andExpect(jsonPath("$.isLinkedToGoogle").value(true));
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found se usuário tentar logar com Google sem estar cadastrado")
        void shouldReturn404WhenGoogleUserIsNotRegistered() throws Exception {
            when(authService.authenticateWithGoogle(any(GoogleAuthRequest.class)))
                    .thenThrow(new BusinessRuleException("error.auth.user_not_found_google", HttpStatus.NOT_FOUND));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleAuthRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}