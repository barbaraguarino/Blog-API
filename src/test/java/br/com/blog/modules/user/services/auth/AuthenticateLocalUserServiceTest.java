package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.AuthResult;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.mappers.UserMapper;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateLocalUserServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthenticateLocalUserService service;

    @Nested
    @DisplayName("Login com e-mail e senha")
    class Authenticate {

        private LoginRequest loginRequest;
        private User mockUser;
        private Authentication mockAuthentication;
        private UserProfileResponse mockUserProfile;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequest("barbara@blog.com", "SenhaForte@123");

            mockUser = User.createLocalUser(
                    "Bárbara",
                    "barbara@blog.com",
                    "senha-criptografada"
            );
            ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());

            mockAuthentication = mock(Authentication.class);

            mockUserProfile = new UserProfileResponse(
                    mockUser.getId(), mockUser.getName(), mockUser.getEmail(),
                    mockUser.getNickname(), mockUser.getRole().name(),
                    false, false, false, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("Deve autenticar com sucesso e retornar token e usuário DTO")
        void shouldAuthenticateSuccessfully() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
            when(mockAuthentication.getName()).thenReturn(loginRequest.email());
            when(tokenService.generateToken(loginRequest.email())).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(mockUser));
            when(userMapper.toResponseDTO(mockUser)).thenReturn(mockUserProfile);

            AuthResult result = service.execute(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.userProfile().email()).isEqualTo("barbara@blog.com");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).generateToken(loginRequest.email());
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException quando credenciais forem inválidas")
        void shouldThrowBadCredentialsWhenLoginFails() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> service.execute(loginRequest));

            verify(tokenService, never()).generateToken(anyString());
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando usuário não for encontrado no banco")
        void shouldThrowResourceNotFoundWhenUserNotFoundInDatabase() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
            when(mockAuthentication.getName()).thenReturn(loginRequest.email());
            when(tokenService.generateToken(loginRequest.email())).thenReturn("token.jwt.valido");
            when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.execute(loginRequest));
        }
    }
}