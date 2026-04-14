package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
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
    @DisplayName("Cadastro de Usuário Padrão")
    class RegisterUser {

        private UserRegistrationDTO dto;
        private User userEntity;
        private User savedUser;

        @BeforeEach
        void setUp() {
            dto = new UserRegistrationDTO("Bárbara", "barbara@grifo.com", "senha123");

            userEntity = new User();
            userEntity.setName(dto.name());
            userEntity.setEmail(dto.email());
            userEntity.setPassword(dto.password());

            savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            savedUser.setName(userEntity.getName());
            savedUser.setEmail(userEntity.getEmail());
            savedUser.setNickname("barbara_1234");
            savedUser.setRole(UserRole.READER);
            savedUser.setPassword("senha-criptografada");
        }

        @Test
        @DisplayName("Deve cadastrar um usuário com sucesso")
        void shouldRegisterUserSuccessfully() {

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(userMapper.toEntity(dto)).thenReturn(userEntity);
            when(passwordEncoder.encode(any())).thenReturn("senha-criptografada");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            User result = userRegistrationService.registerUser(dto);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("barbara@grifo.com");
            assertThat(result.getPassword()).isEqualTo("senha-criptografada");

            verify(passwordEncoder, times(1)).encode("senha123");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já existir")
        void shouldThrowExceptionWhenEmailAlreadyExists() {

            when(userRepository.existsByEmail(dto.email())).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userRegistrationService.registerUser(dto));

            assertThat(exception.getMessageKey()).isEqualTo("error.user.already_exists");
            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Cadastro via Conta Google")
    class RegisterWithGoogle {

        private GoogleTokenDTO requestDTO;
        private GoogleIdToken.Payload payload;
        private GoogleIdToken mockIdToken;
        private User savedUser;
        private final String GOOGLE_TOKEN = "token.falso.do.google";

        @BeforeEach
        void setUp() {
            requestDTO = new GoogleTokenDTO(GOOGLE_TOKEN);

            payload = new GoogleIdToken.Payload();
            payload.setEmail("barbara.google@grifo.com");
            payload.set("name", "Bárbara Google");
            payload.setSubject("google-id-12345");

            mockIdToken = mock(GoogleIdToken.class);

            savedUser = new User();
            savedUser.setId(UUID.randomUUID());
            savedUser.setEmail("barbara.google@grifo.com");
            savedUser.setGoogleId("google-id-12345");
            savedUser.setRole(UserRole.READER);
            savedUser.setCreatedAt(LocalDateTime.now());
        }

        @Test
        @DisplayName("Deve registrar usuário com sucesso via Google e retornar a Entidade")
        void shouldRegisterWithGoogleSuccessfully() throws Exception {
            when(mockIdToken.getPayload()).thenReturn(payload);
            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(mockIdToken);
            when(userRepository.findByEmail("barbara.google@grifo.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            lenient().when(passwordEncoder.encode(anyString())).thenReturn("hash_aleatorio_seguro");

            User result = userRegistrationService.registerWithGoogle(requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("barbara.google@grifo.com");
            assertThat(result.isLinkedToGoogle()).isTrue();

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar UNAUTHORIZED ao receber token do Google forjado/expirado")
        void shouldThrowUnauthorizedWhenGoogleTokenIsInvalid() throws Exception {

            when(googleVerifier.verify(GOOGLE_TOKEN)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> userRegistrationService.registerWithGoogle(requestDTO));

            assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(exception.getMessageKey()).isEqualTo("error.auth.invalid_google_token");

            verify(userRepository, never()).save(any(User.class));
        }
    }
}