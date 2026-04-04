package br.com.grifo.modules.user.services;

import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.dtos.LoginRequestDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public record AuthResult(String token, UserResponseDTO user) {}

    public AuthResult authenticate(LoginRequestDTO dto) {
        var authPasswordToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = authenticationManager.authenticate(authPasswordToken);

        String token = jwtTokenProvider.generateToken(auth.getName());

        User user = userRepository.findByEmail(dto.email()).orElseThrow();
        UserResponseDTO userDTO = userMapper.toResponseDTO(user);
        return new AuthResult(token, userDTO);
    }

}
