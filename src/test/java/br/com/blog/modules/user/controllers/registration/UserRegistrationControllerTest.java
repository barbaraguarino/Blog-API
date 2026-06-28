package br.com.blog.modules.user.controllers.registration;

import br.com.blog.core.exceptions.BusinessException;
import br.com.blog.core.security.CustomUserDetailsService;
import br.com.blog.core.security.JwtAuthenticationFilter;
import br.com.blog.core.security.JwtTokenProvider;
import br.com.blog.core.security.SecurityConfig;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleTokenDTO;
import br.com.blog.modules.user.dtos.registration.UserRegistrationDTO;
import br.com.blog.modules.user.dtos.shared.UserResponseDTO;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.services.registration.UserRegistrationService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRegistrationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User userSaved;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userSaved = User.createLocalUser(
                "Bárbara Nascimento",
                "barbara@grifo.com",
                "senha-criptografada",
                "barbara_nascimento_1234"
        );

        ReflectionTestUtils.setField(userSaved, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userSaved, "googleId", "token.valido.do.google");
        ReflectionTestUtils.setField(userSaved, "createdAt", LocalDateTime.now());

        userResponseDTO = new UserResponseDTO(
                userSaved.getId(),
                userSaved.getName(),
                userSaved.getEmail(),
                userSaved.getNickname(),
                userSaved.getRole().toString(),
                false,
                false,
                false,
                userSaved.getCreatedAt()
        );
    }

    @Nested
    @DisplayName("Cadastro simples (E-mail e senha)")
    class Register {

        private UserRegistrationDTO registrationDTO;

        @BeforeEach
        void setUp() {
            registrationDTO = new UserRegistrationDTO("Bárbara Nascimento", "barbara@grifo.com", "SenhaForte@123");
        }

        @Test
        @DisplayName("Deve retornar 201 e dados do usuário quando payload é válido")
        void shouldReturn201WhenPayloadIsValid() throws Exception {

            when(userRegistrationService.registerUser(any(UserRegistrationDTO.class))).thenReturn(userSaved);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nickname").value("barbara_nascimento_1234"))
                    .andExpect(jsonPath("$.password").doesNotExist());
        }

        @Test
        @DisplayName("Deve retornar 409 quando e-mail já existe")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {

            when(userRegistrationService.registerUser(any(UserRegistrationDTO.class)))
                    .thenThrow(new BusinessException("error.user.already_exists", HttpStatus.CONFLICT));

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Deve retornar 400 quando a senha é fraca")
        void shouldReturn400WhenPasswordIsWeak() throws Exception {

            UserRegistrationDTO weakPasswordDTO = new UserRegistrationDTO("Bárbara", "barbara@grifo.com", "123");

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(weakPasswordDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Cadastro via Google")
    class RegisterWithGoogle {

        private GoogleTokenDTO googleTokenDTO;

        @BeforeEach
        void setUp() {
            googleTokenDTO = new GoogleTokenDTO("token-valido-google");
        }

        @Test
        @DisplayName("Deve retornar 201 e dados do usuário quando token Google é válido")
        void shouldReturn201WhenGoogleTokenIsValid() throws Exception {

            when(userRegistrationService.registerWithGoogle(any(GoogleTokenDTO.class))).thenReturn(userSaved);
            when(userMapper.toResponseDTO(any(User.class))).thenReturn(userResponseDTO);

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleTokenDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.email").value("barbara@grifo.com"));
        }

        @Test
        @DisplayName("Deve retornar 409 se usuário Google já estiver cadastrado")
        void shouldReturn409WhenGoogleUserAlreadyExists() throws Exception {

            when(userRegistrationService.registerWithGoogle(any(GoogleTokenDTO.class)))
                    .thenThrow(new BusinessException("error.user.already_exists", HttpStatus.CONFLICT));

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(googleTokenDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("Deve retornar 400 se o token Google estiver vazio")
        void shouldReturn400WhenGoogleTokenIsEmpty() throws Exception {

            GoogleTokenDTO emptyTokenDTO = new GoogleTokenDTO("");

            mockMvc.perform(post("/api/v1/register/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyTokenDTO)))
                    .andExpect(status().isBadRequest());
        }
    }
}