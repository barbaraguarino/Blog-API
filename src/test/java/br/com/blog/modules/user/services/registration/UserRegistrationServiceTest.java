package br.com.blog.modules.user.services.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private GoogleIdTokenVerifier googleVerifier;
    @InjectMocks private UserRegistrationService userRegistrationService;

    @Nested
    @DisplayName("Cadastro de Usuário Padrão")
    class RegisterUser {

        private RegisterUserRequest dto;

        @BeforeEach
        void setUp() {
            dto = new RegisterUserRequest("Bárbara", "barbara@blog.com", "SenhaForte@123");
        }

        @Test
        @DisplayName("Deve cadastrar usuário com sucesso")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");

            userRegistrationService.registerUser(dto);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar ResourceAlreadyExistsException quando e-mail já existir")
        void shouldThrowResourceAlreadyExistsWhenEmailExists() {
            when(userRepository.existsByEmail(dto.email())).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> userRegistrationService.registerUser(dto));
        }
    }

    @Nested
    @DisplayName("Cadastro com Conta Google")
    class RegisterWithGoogle {

        private final String GOOGLE_TOKEN = "valid.token";
        private GoogleAuthRequest requestDTO;
        private GoogleIdToken mockIdToken;
        private GoogleIdToken.Payload payload;

        @BeforeEach
        void setUp() {
            requestDTO = new GoogleAuthRequest(GOOGLE_TOKEN);
            payload = new GoogleIdToken.Payload();
            payload.setEmail("google@blog.com");
            payload.setSubject("google-id-123");
            payload.set("name", "Bárbara Google");

            mockIdToken = mock(GoogleIdToken.class);
        }

        @Test
        @DisplayName("Deve registrar usuário Google com sucesso")
        void shouldRegisterWithGoogleSuccessfully() throws Exception {
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByGoogleId(anyString())).thenReturn(false);

            userRegistrationService.registerWithGoogle(requestDTO);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException quando token for nulo")
        void shouldThrowBadCredentialsWhenTokenIsNull() throws Exception {
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(null);

            assertThrows(BadCredentialsException.class, () -> userRegistrationService.registerWithGoogle(requestDTO));
        }

        @Test
        @DisplayName("Deve lançar ResourceAlreadyExistsException quando e-mail já existir")
        void shouldThrowResourceAlreadyExistsWhenEmailTaken() throws Exception {
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(userRepository.existsByEmail(payload.getEmail())).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> userRegistrationService.registerWithGoogle(requestDTO));
        }

        @Test
        @DisplayName("Deve lançar ResourceAlreadyExistsException quando Google ID já existir")
        void shouldThrowResourceAlreadyExistsWhenGoogleIdTaken() throws Exception {
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByGoogleId(payload.getSubject())).thenReturn(true);

            assertThrows(ResourceAlreadyExistsException.class, () -> userRegistrationService.registerWithGoogle(requestDTO));
        }
    }
}