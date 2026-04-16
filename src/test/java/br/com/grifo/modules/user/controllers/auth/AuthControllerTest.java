package br.com.grifo.modules.user.controllers.auth;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.core.security.CustomUserDetailsService;
import br.com.grifo.core.security.JwtAuthenticationFilter;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.core.security.SecurityConfig;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.dtos.auth.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.auth.LoginRequestDTO;
import br.com.grifo.modules.user.dtos.shared.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.services.auth.AuthService;
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
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User savedUser;
    private UserResponseDTO savedUserResponseDTO;

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

        savedUserResponseDTO = new UserResponseDTO(
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

        private LoginRequestDTO loginRequestDTO;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            loginRequestDTO = new LoginRequestDTO("barbara@grifo.com", "SenhaForte@123");
            authResult = new AuthService.AuthResult("token.jwt.falso", savedUser);
        }

        @Test
        @DisplayName("Deve retornar 200 OK, Cookie HttpOnly e DTO do usuário ao logar com sucesso")
        void shouldReturn200AndCookieWhenLoginIsSuccessful() throws Exception {
            when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserResponseDTO);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
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

            when(authService.authenticate(any(LoginRequestDTO.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDTO)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Login com conta Google")
    class LoginWithGoogle {

        private GoogleTokenDTO googleTokenDTO;
        private AuthService.AuthResult authResult;

        @BeforeEach
        void setUp() {
            googleTokenDTO = new GoogleTokenDTO("token.valido.do.google");
            authResult = new AuthService.AuthResult("token.jwt.google.falso", savedUser);
        }

        @Test
        @DisplayName("Deve retornar 200 OK e Cookie HttpOnly ao logar com Google com sucesso")
        void shouldReturn200AndCookieWhenGoogleLoginIsSuccessful() throws Exception {
            when(authService.authenticateWithGoogle(any(GoogleTokenDTO.class))).thenReturn(authResult);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(savedUserResponseDTO);

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleTokenDTO)))
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
            when(authService.authenticateWithGoogle(any(GoogleTokenDTO.class)))
                    .thenThrow(new BusinessException("error.auth.user_not_found_google", HttpStatus.NOT_FOUND));

            mockMvc.perform(post("/api/v1/auth/login/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleTokenDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}