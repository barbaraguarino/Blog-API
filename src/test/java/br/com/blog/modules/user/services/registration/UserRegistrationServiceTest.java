package br.com.blog.modules.user.services.registration;

import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private GoogleIdTokenVerifier googleVerifier;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Nested
    @DisplayName("Cadastro de Usuário Padrão")
    class RegisterUser {

        private RegisterUserRequest dto;
        private User savedUser;

        @BeforeEach
        void setUp() {
            dto = new RegisterUserRequest("Bárbara", "barbara@grifo.com", "senha123");

            savedUser = User.createLocalUser(
                    dto.name(),
                    dto.email(),
                    "senha-criptografada",
                    "barbara_nickname"
            );
        }

        @Test
        @DisplayName("Deve cadastrar um usuário com sucesso")
        void shouldRegisterUserSuccessfully() {

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userRegistrationService.registerUser(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("barbara@grifo.com");
            assertThat(result.getPassword()).isEqualTo("senha-criptografada");

            verify(passwordEncoder, times(1)).encode("senha123");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessRuleException quando email já existir")
        void shouldThrowExceptionWhenEmailAlreadyExists() {

        }
    }

    @Nested
    @DisplayName("Cadastro via Conta Google")
    class RegisterWithGoogle {

        private GoogleAuthRequest requestDTO;
        private GoogleIdToken.Payload payload;
        private GoogleIdToken mockIdToken;
        private User savedUser;
        private final String GOOGLE_TOKEN = "token.falso.do.google";

        @BeforeEach
        void setUp() {
            requestDTO = new GoogleAuthRequest(GOOGLE_TOKEN);

            payload = new GoogleIdToken.Payload();
            payload.setEmail("barbara.google@grifo.com");
            payload.set("name", "Bárbara Google");
            payload.setSubject("google-id-12345");

            mockIdToken = mock(GoogleIdToken.class);

            savedUser = User.createGoogleUser(
                    "Bárbara Google",
                    "barbara.google@grifo.com",
                    "google-id-12345",
                    "barbara_google"
            );
        }

        @Test
        @DisplayName("Deve registrar usuário com sucesso via Google e retornar a Entidade")
        void shouldRegisterWithGoogleSuccessfully() throws Exception {

            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(userRepository.existsByEmail("barbara.google@grifo.com")).thenReturn(false);
            when(userRepository.existsByGoogleId("google-id-12345")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userRegistrationService.registerWithGoogle(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.isLinkedToGoogle()).isTrue();

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar UNAUTHORIZED ao receber token do Google forjado/expirado")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() {

        }
    }
}