package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.LoginRequestDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login com e-mail e senha")
    class SimpleLogin{

        @Test
        @DisplayName("Deve autenticar e retornar AuthResult com Token e DTO")
        void shouldAuthenticateAndReturnAuthResult() {
            LoginRequestDTO dto = new LoginRequestDTO("barbara@grifo.com", "senha123");
            Authentication authMock = mock(Authentication.class);
            when(authMock.getName()).thenReturn("barbara@grifo.com");
            User fakeUser = new User();
            fakeUser.setId(UUID.randomUUID());
            fakeUser.setEmail("barbara@grifo.com");
            fakeUser.setRole(UserRole.READER);
            UserResponseDTO responseDTO = new UserResponseDTO(
                    fakeUser.getId(), "Bárbara", "barbara@grifo.com", "barbara_1234",
                    "READER", false, true, false, LocalDateTime.now()
            );
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
            when(jwtTokenProvider.generateToken("barbara@grifo.com")).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail("barbara@grifo.com")).thenReturn(Optional.of(fakeUser));
            when(userMapper.toResponseDTO(fakeUser)).thenReturn(responseDTO);
            AuthService.AuthResult result = authService.authenticate(dto);
            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.user().email()).isEqualTo("barbara@grifo.com");
            verify(authenticationManager, times(1)).authenticate(any());
            verify(jwtTokenProvider, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve estourar BadCredentialsException quando senha for invalida")
        void shouldThrowExceptionWhenPasswordIsInvalid() {
            LoginRequestDTO dto = new LoginRequestDTO("barbara@grifo.com", "senha-errada");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            assertThrows(BadCredentialsException.class, () -> authService.authenticate(dto));
            verify(jwtTokenProvider, never()).generateToken(anyString());
        }

    }

    @Nested
    @DisplayName("Login via Google")
    class LoginWithGoogle {

        @Test
        @DisplayName("Deve autenticar via Google e retornar AuthResult com Token")
        void shouldAuthenticateWithGoogleSuccessfully() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.falso.do.google");

            GoogleIdToken.Payload payload =
                    new GoogleIdToken.Payload();
            payload.setSubject("google-id-12345");
            payload.setEmail("barbara.google@grifo.com");

            GoogleIdToken mockIdToken =
                    mock(GoogleIdToken.class);

            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify("token.falso.do.google")).thenReturn(mockIdToken);

            User fakeUser = new User();
            fakeUser.setId(UUID.randomUUID());
            fakeUser.setEmail("barbara.google@grifo.com");
            fakeUser.setGoogleId("google-id-12345");
            fakeUser.setRole(UserRole.READER);

            UserResponseDTO responseDTO = new UserResponseDTO(
                    fakeUser.getId(), "Bárbara Google", "barbara.google@grifo.com", "barbara_g",
                    "READER", true, true, false, LocalDateTime.now()
            );

            when(userRepository.findByGoogleId("google-id-12345")).thenReturn(Optional.of(fakeUser));
            when(jwtTokenProvider.generateToken("barbara.google@grifo.com")).thenReturn("token.jwt.google");
            when(userMapper.toResponseDTO(fakeUser)).thenReturn(responseDTO);

            AuthService.AuthResult result = authService.authenticateWithGoogle(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.google");
            assertThat(result.user().email()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.user().isLinkedToGoogle()).isTrue();

            verify(jwtTokenProvider, times(1)).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessException (NOT FOUND) quando usuário Google não existir")
        void shouldThrowNotFoundWhenGoogleUserDoesNotExist() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.falso");

            GoogleIdToken.Payload payload =
                    new GoogleIdToken.Payload();
            payload.setSubject("google-id-inexistente");

            GoogleIdToken mockIdToken =
                    mock(GoogleIdToken.class);

            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify("token.falso")).thenReturn(mockIdToken);

            when(userRepository.findByGoogleId("google-id-inexistente")).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.authenticateWithGoogle(requestDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.user_not_found_google");
            verify(jwtTokenProvider, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve retornar BusinessException (UNAUTHORIZED) ao receber token do Google forjado")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.expirado");
            when(googleVerifier.verify("token.expirado")).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> authService.authenticateWithGoogle(requestDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.invalid_google_token");
        }
    }

}