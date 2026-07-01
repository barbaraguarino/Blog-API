package br.com.blog.modules.user.services.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterLocalUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private RegisterLocalUserService service;

    private RegisterUserRequest requestDTO;
    private User mockUser;
    private UserProfileResponse mockUserProfile;

    @BeforeEach
    void setUp() {
        requestDTO = new RegisterUserRequest("Bárbara", "barbara@blog.com", "SenhaForte@123");

        mockUser = User.createLocalUser("Bárbara", "barbara@blog.com", "encoded-password");
        ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());

        mockUserProfile = new UserProfileResponse(
                mockUser.getId(), mockUser.getName(), mockUser.getEmail(),
                mockUser.getNickname(), mockUser.getRole().name(),
                false, false, false, LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso e retornar DTO")
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(requestDTO.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userMapper.toResponseDTO(mockUser)).thenReturn(mockUserProfile);

        UserProfileResponse result = service.execute(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("barbara@blog.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceAlreadyExistsException quando e-mail já existir")
    void shouldThrowResourceAlreadyExistsWhenEmailExists() {
        when(userRepository.existsByEmail(requestDTO.email())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> service.execute(requestDTO));
        verify(userRepository, never()).save(any(User.class));
    }
}