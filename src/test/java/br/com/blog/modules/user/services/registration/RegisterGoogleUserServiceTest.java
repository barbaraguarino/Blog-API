package br.com.blog.modules.user.services.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.core.exceptions.infrastructure.ExternalProviderAuthException;
import br.com.blog.infrastructure.providers.google.GoogleAuthGateway;
import br.com.blog.modules.user.application.usecases.registration.RegisterGoogleUserService;
import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.auth.GoogleAuthRequestDTO;
import br.com.blog.modules.user.application.dtos.auth.internal.GoogleUserInfoDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.mappers.UserMapper;
import br.com.blog.modules.user.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterGoogleUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private GoogleAuthGateway googleAuthGateway;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private RegisterGoogleUserService service;

    private final String GOOGLE_TOKEN = "valid.token";
    private GoogleAuthRequestDTO requestDTO;
    private GoogleUserInfoDTO mockGoogleUser;
    private User mockSavedUser;
    private UserProfileResponseDTO mockUserProfile;

    @BeforeEach
    void setUp() {
        requestDTO = new GoogleAuthRequestDTO(GOOGLE_TOKEN);
        mockGoogleUser = new GoogleUserInfoDTO("google-id-123", "google@blog.com", "Bárbara Google");

        mockSavedUser = User.createGoogleUser("Bárbara Google", "google@blog.com", "google-id-123");
        ReflectionTestUtils.setField(mockSavedUser, "id", UUID.randomUUID());

        mockUserProfile = new UserProfileResponseDTO(
                mockSavedUser.getId(), mockSavedUser.getName(), mockSavedUser.getEmail(),
                mockSavedUser.getNickname(), mockSavedUser.getRole().name(),
                true, false, false, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve registrar usuário Google com sucesso e retornar DTO")
    void shouldRegisterWithGoogleSuccessfully(){
        when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByGoogleId(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);
        when(userMapper.toResponseDTO(mockSavedUser)).thenReturn(mockUserProfile);

        UserProfileResponseDTO result = service.execute(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("google@blog.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar ExternalProviderAuthException quando token for nulo ou inválido")
    void shouldThrowExternalProviderAuthWhenTokenIsInvalid(){
        when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN))
                .thenThrow(new ExternalProviderAuthException("error.auth.google_token_invalid"));

        assertThrows(ExternalProviderAuthException.class, () -> service.execute(requestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceAlreadyExistsException quando e-mail já existir")
    void shouldThrowResourceAlreadyExistsWhenEmailTaken(){
        when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUser);
        when(userRepository.existsByEmail(mockGoogleUser.email())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> service.execute(requestDTO));
    }

    @Test
    @DisplayName("Deve lançar ResourceAlreadyExistsException quando Google ID já existir")
    void shouldThrowResourceAlreadyExistsWhenGoogleIdTaken(){
        when(googleAuthGateway.extractUserInfo(GOOGLE_TOKEN)).thenReturn(mockGoogleUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByGoogleId(mockGoogleUser.googleId())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> service.execute(requestDTO));
    }
}