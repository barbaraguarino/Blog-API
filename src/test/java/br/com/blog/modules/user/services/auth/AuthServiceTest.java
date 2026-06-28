package br.com.blog.modules.user.services.auth;

import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private GoogleIdTokenVerifier googleVerifier;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Authenticate {

        private LoginRequest loginDTO;
        private User fakeUser;
        private Authentication authMock;

        @BeforeEach
        void setUp() {
            loginDTO = new LoginRequest("barbara@grifo.com", "senha123");

            fakeUser = User.createLocalUser(
                    "Barbara Nascimento",
                    "barbara@grifo.com",
                    "Forte!123",
                    "barbara_nascimento_123"
            );

            authMock = mock(Authentication.class);
        }

        @Test
        @DisplayName("Deve autenticar e retornar AuthResult com Token e DTO")
        void shouldAuthenticateAndReturnAuthResult() {

            when(authMock.getName()).thenReturn("barbara@grifo.com");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
            when(tokenService.generateToken("barbara@grifo.com")).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail("barbara@grifo.com")).thenReturn(Optional.of(fakeUser));

            AuthService.AuthResult result = authService.authenticate(loginDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.user().getEmail()).isEqualTo("barbara@grifo.com");

            verify(authenticationManager, times(1)).authenticate(any());
            verify(tokenService, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve estourar BadCredentialsException quando credenciais forem inválidas")
        void shouldThrowExceptionWhenCredentialsAreInvalid() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginDTO));

            verify(tokenService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessRuleException (NOT FOUND) se o usuário não for encontrado após autenticação validada")
        void shouldThrowNotFoundWhenUserDoesNotExistAfterAuthentication() {

        }
    }

    @Nested
    @DisplayName("Login via Google")
    class AuthenticateWithGoogle {

        private GoogleAuthRequest googleDTO;
        private GoogleIdToken mockIdToken;
        private GoogleIdToken.Payload payload;
        private User fakeUser;
        private final String GOOGLE_TOKEN = "token.jwt.do.google";

        @BeforeEach
        void setUp() {
            googleDTO = new GoogleAuthRequest(GOOGLE_TOKEN);

            payload = new GoogleIdToken.Payload();
            payload.setSubject("google-id-12345");
            payload.setEmail("barbara.google@grifo.com");

            mockIdToken = mock(GoogleIdToken.class);

            fakeUser = User.createGoogleUser(
                    "Barbara Nascimento",
                    "barbara.google@grifo.com",
                    "google-id-12345",
                    "barbara_nascimento_123"
            );

            ReflectionTestUtils.setField(fakeUser, "googleId", GOOGLE_TOKEN);

        }

        @Test
        @DisplayName("Deve autenticar via Google e retornar AuthResult com Token")
        void shouldAuthenticateWithGoogleSuccessfully() throws Exception {
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(userRepository.findByGoogleId("google-id-12345")).thenReturn(Optional.of(fakeUser));
            when(tokenService.generateToken("barbara.google@grifo.com")).thenReturn("token.jwt.gerado");

            AuthService.AuthResult result = authService.authenticateWithGoogle(googleDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.gerado");
            assertThat(result.user().getEmail()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.user().isLinkedToGoogle()).isTrue();

            verify(tokenService, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessRuleException (NOT FOUND) quando usuário Google não existir")
        void shouldThrowNotFoundWhenGoogleUserDoesNotExist(){

        }

        @Test
        @DisplayName("Deve retornar BusinessRuleException (UNAUTHORIZED) ao receber token do Google forjado/inválido")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() {

        }
    }
}