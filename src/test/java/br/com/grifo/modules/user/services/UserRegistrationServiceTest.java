package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GoogleIdTokenVerifier googleVerifier;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Nested
    class SimpleRegistration{

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
                    "READER", false, true, false, LocalDateTime.now()
            );

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(userMapper.toEntity(dto)).thenReturn(userEntity);
            when(passwordEncoder.encode(any())).thenReturn("senha-criptografada");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponseDTO(savedUser)).thenReturn(responseDTO);

            UserResponseDTO result = userRegistrationService.registerUser(dto);

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

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userRegistrationService.registerUser(dto));

            assertThat(exception.getMessage()).isEqualTo("error.user.already_exists");
            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any(User.class));
        }

    }

    @Nested
    class RegisterWithGoogle{

        @Test
        @DisplayName("Deve registrar usuário com sucesso via Google e retornar DTO")
        void shouldRegisterWithGoogleSuccessfully() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.falso.do.google");

            GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
            payload.setEmail("barbara.google@grifo.com");
            payload.set("name", "Bárbara Google");
            payload.setSubject("google-id-12345");

            GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
            when(mockIdToken.getPayload()).thenReturn(payload);

            when(googleVerifier.verify("token.falso.do.google")).thenReturn(mockIdToken);
            when(userRepository.findByEmail("barbara.google@grifo.com")).thenReturn(Optional.empty());

            lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash_aleatorio_seguro");

            User savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            savedUser.setEmail("barbara.google@grifo.com");

            UserResponseDTO responseDTO = new UserResponseDTO(
                    savedUser.getId(), "Bárbara Google", "barbara.google@grifo.com", "barbara_google_123", "READER", true, true, false, LocalDateTime.now()
            );

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            when(userMapper.toResponseDTO(any(User.class))).thenReturn(responseDTO);

            UserResponseDTO result = userRegistrationService.registerWithGoogle(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.isLinkedToGoogle()).isTrue();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar UNAUTHORIZED ao receber token do Google forjado/expirado")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {
            GoogleTokenDTO requestDTO = new GoogleTokenDTO("token.expirado.ou.forjado");

            when(googleVerifier.verify("token.expirado.ou.forjado")).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userRegistrationService.registerWithGoogle(requestDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.invalid_google_token");
        }

    }


}