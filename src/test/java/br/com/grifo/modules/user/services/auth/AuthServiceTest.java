package br.com.grifo.modules.user.services.auth;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.dtos.auth.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.auth.LoginRequestDTO;
import br.com.grifo.modules.user.repositories.UserRepository;
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
import org.springframework.http.HttpStatus;
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
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GoogleIdTokenVerifier googleVerifier;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Authenticate {

        private LoginRequestDTO loginDTO;
        private User fakeUser;
        private Authentication authMock;

        @BeforeEach
        void setUp() {
            loginDTO = new LoginRequestDTO("barbara@grifo.com", "senha123");

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
            when(jwtTokenProvider.generateToken("barbara@grifo.com")).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail("barbara@grifo.com")).thenReturn(Optional.of(fakeUser));

            AuthService.AuthResult result = authService.authenticate(loginDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.user().getEmail()).isEqualTo("barbara@grifo.com");

            verify(authenticationManager, times(1)).authenticate(any());
            verify(jwtTokenProvider, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve estourar BadCredentialsException quando credenciais forem inválidas")
        void shouldThrowExceptionWhenCredentialsAreInvalid() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginDTO));

            verify(jwtTokenProvider, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessException (NOT FOUND) se o usuário não for encontrado após autenticação validada")
        void shouldThrowNotFoundWhenUserDoesNotExistAfterAuthentication() {
            when(authMock.getName()).thenReturn("barbara@grifo.com");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);

            when(jwtTokenProvider.generateToken("barbara@grifo.com")).thenReturn("token.jwt.valido");

            when(userRepository.findByEmail("barbara@grifo.com")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.authenticate(loginDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.user_not_found");

            verify(authenticationManager, times(1)).authenticate(any());
            verify(jwtTokenProvider, times(1)).generateToken(anyString());
        }
    }

    @Nested
    @DisplayName("Login via Google")
    class AuthenticateWithGoogle {

        private GoogleTokenDTO googleDTO;
        private GoogleIdToken mockIdToken;
        private GoogleIdToken.Payload payload;
        private User fakeUser;
        private final String GOOGLE_TOKEN = "token.jwt.do.google";

        @BeforeEach
        void setUp() {
            googleDTO = new GoogleTokenDTO(GOOGLE_TOKEN);

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
            when(jwtTokenProvider.generateToken("barbara.google@grifo.com")).thenReturn("token.jwt.gerado");

            AuthService.AuthResult result = authService.authenticateWithGoogle(googleDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.gerado");
            assertThat(result.user().getEmail()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.user().isLinkedToGoogle()).isTrue();

            verify(jwtTokenProvider, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessException (NOT FOUND) quando usuário Google não existir")
        void shouldThrowNotFoundWhenGoogleUserDoesNotExist() throws Exception {
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(userRepository.findByGoogleId("google-id-12345")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.authenticateWithGoogle(googleDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.user_not_found_google");

            verify(jwtTokenProvider, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessException (UNAUTHORIZED) ao receber token do Google forjado/inválido")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.authenticateWithGoogle(googleDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.invalid_google_token");

            verify(jwtTokenProvider, never()).generateToken(anyString());
        }
    }
}