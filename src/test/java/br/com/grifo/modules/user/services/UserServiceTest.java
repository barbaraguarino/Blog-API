package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Deve cadastrar um usuário com sucesso")
    void shouldRegisterUserSuccessfully() {
        UserRegistrationDTO dto = new UserRegistrationDTO("Bárbara", "barbara@grifo.com", "senha123");

        User userEntity = new User();
        userEntity.setName(dto.name());
        userEntity.setEmail(dto.email());
        userEntity.setPassword(dto.password());

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setName(userEntity.getName());
        savedUser.setNickname("barbara_1234");
        savedUser.setRole(UserRole.READER);

        UserResponseDTO responseDTO = new UserResponseDTO(
                savedUser.getId(), "Bárbara", "barbara@grifo.com", "barbara_1234",
                "READER", true, false, LocalDateTime.now()
        );

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(passwordEncoder.encode(any())).thenReturn("senha-criptografada");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponseDTO(savedUser)).thenReturn(responseDTO);

        UserResponseDTO result = userService.registerUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("barbara@grifo.com");

        verify(passwordEncoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando email ja existir")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserRegistrationDTO dto = new UserRegistrationDTO("Bárbara", "existente@grifo.com", "senha123");

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.registerUser(dto);
        });

        assertThat(exception.getMessage()).isEqualTo("error.user.already_exists");
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(userRepository, never()).save(any(User.class));
    }

}