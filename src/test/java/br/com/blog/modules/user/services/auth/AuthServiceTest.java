package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.GoogleAuthGateway;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.GoogleUserInfo;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.repositories.UserRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private GoogleAuthGateway googleAuthGateway;

    @InjectMocks private AuthService authService;

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Authenticate {

        private LoginRequest loginRequest;
        private User mockUser;
        private Authentication mockAuthentication;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest("barbara@blog.com", "SenhaForte@123");

            mockUser = User.createLocalUser(
                    "Bárbara",
                    "barbara@blog.com",
                    "senha-criptografada"
            );

            mockAuthentication = mock(Authentication.class);
        }

        @Test
        @DisplayName("Deve autenticar com sucesso e retornar token e usuário")
        void shouldAuthenticateSuccessfully() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
            when(mockAuthentication.getName()).thenReturn(loginRequest.email());
            when(tokenService.generateToken(loginRequest.email())).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));

            AuthService.AuthResult result = authService.authenticate(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.user().getEmail()).isEqualTo("barbara@blog.com");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).generateToken(loginRequest.email());
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException quando credenciais forem inválidas")
        void shouldThrowBadCredentialsWhenLoginFails() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticate(loginRequest));

            verify(tokenService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando usuário não for encontrado no banco")
        void shouldThrowResourceNotFoundWhenUserNotFoundInDatabase() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
            when(mockAuthentication.getName()).thenReturn(loginRequest.email());
            when(tokenService.generateToken(loginRequest.email())).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.authenticate(loginRequest));
        }
    }

    @Nested
    @DisplayName("Login com Conta do Google")
    class AuthenticateWithGoogle {

        private final String GOOGLE_TOKEN = "google.token.valido";
        private GoogleAuthRequest requestDTO;
        private GoogleUserInfo mockGoogleUserInfo;
        private User mockGoogleUser;

        @BeforeEach
        void setUp() {
            requestDTO = new GoogleAuthRequest(GOOGLE_TOKEN);

            mockGoogleUserInfo = new GoogleUserInfo("google-id-123", "google@blog.com", "Bárbara Google");

            mockGoogleUser = User.createGoogleUser(
                    "Bárbara Google",
                    "google@blog.com",
                    "google-id-123"
            );
        }

        @Test
        @DisplayName("Deve logar com sucesso via Google e retornar token gerado")
        void shouldAuthenticateWithGoogleSuccessfully() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUserInfo);
            when(userRepository.findByGoogleId(mockGoogleUserInfo.googleId())).thenReturn(Optional.of(mockGoogleUser));
            when(tokenService.generateToken("google@blog.com")).thenReturn("token.jwt.gerado");

            AuthService.AuthResult result = authService.authenticateWithGoogle(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.gerado");
            assertThat(result.user().getEmail()).isEqualTo("google@blog.com");
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException quando token Google for nulo/inválido")
        void shouldThrowBadCredentialsWhenTokenIsInvalid() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN))
                    .thenThrow(new BadCredentialsException("error.auth.google_token_invalid"));

            assertThrows(BadCredentialsException.class, () -> authService.authenticateWithGoogle(requestDTO));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando conta Google não estiver registrada")
        void shouldThrowResourceNotFoundWhenUserNotFoundInDatabase() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUserInfo);
            when(userRepository.findByGoogleId(mockGoogleUserInfo.googleId())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.authenticateWithGoogle(requestDTO));
        }
    }
}