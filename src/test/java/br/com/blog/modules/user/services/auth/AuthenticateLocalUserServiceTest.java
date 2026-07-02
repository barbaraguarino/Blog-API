package br.com.blog.modules.user.services.auth;

import br.com.blog.core.security.jwt.TokenService;
import br.com.blog.core.security.userdetails.CustomUserDetails;
import br.com.blog.modules.user.application.usecases.auth.AuthenticateLocalUserService;
import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.auth.internal.AuthResultDTO;
import br.com.blog.modules.user.application.dtos.auth.LoginRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.mappers.UserMapper;
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
    @Mock private UserMapper userMapper;

    @InjectMocks private AuthenticateLocalUserService service;

    @Nested
    @DisplayName("Login com Credenciais Locais (E-mail ou Nickname)")
    class Authenticate {

        private LoginRequestDTO loginRequest;
        private User mockUser;
        private CustomUserDetails mockUserDetails;
        private Authentication mockAuthentication;
        private UserProfileResponseDTO mockUserProfile;

        @BeforeEach
        void setUp() {
            loginRequest = new LoginRequestDTO("barbara_1234", "SenhaForte@123");

            mockUser = User.createLocalUser("Bárbara", "barbara@blog.com", "senha-criptografada");
            ReflectionTestUtils.setField(mockUser, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(mockUser, "nickname", "barbara_1234");

            mockUserDetails = new CustomUserDetails(mockUser);
            mockAuthentication = mock(Authentication.class);

            mockUserProfile = new UserProfileResponseDTO(
                    mockUser.getId(), mockUser.getName(), mockUser.getEmail(),
                    mockUser.getNickname(), mockUser.getRole().name(),
                    false, false, false, LocalDateTime.now()
            );
        }

        @Test
        @DisplayName("Deve autenticar com sucesso extraindo o User da memória")
        void shouldAuthenticateSuccessfully() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(mockAuthentication);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
            when(tokenService.generateToken(mockUser.getEmail())).thenReturn("token.jwt.valido");
            when(userMapper.toResponseDTO(mockUser)).thenReturn(mockUserProfile);

            AuthResultDTO result = service.execute(loginRequest);

            assertThat(result).isNotNull();
            assertThat(result.token()).isEqualTo("token.jwt.valido");
            assertThat(result.userProfile().nickname()).isEqualTo("barbara_1234");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenService).generateToken(mockUser.getEmail());
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException quando as credenciais forem inválidas")
        void shouldThrowBadCredentialsWhenLoginFails() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThrows(BadCredentialsException.class, () -> service.execute(loginRequest));

            verify(tokenService, never()).generateToken(anyString());
        }
    }
}