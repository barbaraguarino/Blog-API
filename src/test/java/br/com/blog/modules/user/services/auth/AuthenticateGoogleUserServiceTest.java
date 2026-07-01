package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.exceptions.infrastructure.ExternalProviderAuthException;
import br.com.blog.core.security.GoogleAuthGateway;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.AuthResult;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.GoogleUserInfo;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateGoogleUserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private TokenService tokenService;
    @Mock private UserRepository userRepository;
    @Mock private GoogleAuthGateway googleAuthGateway;

    @InjectMocks
    private AuthenticateGoogleUserService service;

    @Nested
    @DisplayName("Login com Conta do Google")
    class AuthenticateWithGoogle {

        private final String GOOGLE_TOKEN = "google.token.valido";
        private GoogleAuthRequest requestDTO;
        private GoogleUserInfo mockGoogleUserInfo;
        private User mockGoogleUser;
        private UserProfileResponse mockUserProfile;

        @BeforeEach
        void setUp() {
            requestDTO = new GoogleAuthRequest(GOOGLE_TOKEN);

            mockGoogleUserInfo = new GoogleUserInfo("google-id-123", "google@blog.com", "Bárbara Google");

            mockGoogleUser = User.createGoogleUser(
                    "Bárbara Google",
                    "google@blog.com",
                    "google-id-123"
            );
            ReflectionTestUtils.setField(mockGoogleUser, "id", UUID.randomUUID());

            mockUserProfile = new UserProfileResponse(
                    mockGoogleUser.getId(), mockGoogleUser.getName(), mockGoogleUser.getEmail(),
                    mockGoogleUser.getNickname(), mockGoogleUser.getRole().name(),
                    true, false, false, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("Deve logar com sucesso via Google e retornar token gerado e perfil DTO")
        void shouldAuthenticateWithGoogleSuccessfully() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUserInfo);
            when(userRepository.findByGoogleId(mockGoogleUserInfo.googleId())).thenReturn(Optional.of(mockGoogleUser));
            when(tokenService.generateToken("google@blog.com")).thenReturn("token.jwt.gerado");
            when(userMapper.toResponseDTO(mockGoogleUser)).thenReturn(mockUserProfile);

            AuthResult result = service.execute(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.gerado");
            assertThat(result.userProfile().email()).isEqualTo("google@blog.com");
        }

        @Test
        @DisplayName("Deve lançar ExternalProviderAuthException quando token Google for nulo/inválido")
        void shouldThrowExternalProviderAuthWhenTokenIsInvalid() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN))
                    .thenThrow(new ExternalProviderAuthException("error.auth.google_token_invalid"));

            assertThrows(ExternalProviderAuthException.class, () -> service.execute(requestDTO));
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando conta Google não estiver registrada")
        void shouldThrowResourceNotFoundWhenUserNotFoundInDatabase() {
            when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUserInfo);
            when(userRepository.findByGoogleId(mockGoogleUserInfo.googleId())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> service.execute(requestDTO));
        }
    }
}