package br.com.grifo.modules.user.services;

import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.LoginRequestDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

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
                "READER", true, false, LocalDateTime.now()
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