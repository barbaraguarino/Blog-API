package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.AuthResult;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticateLocalUserService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AuthResult execute(LoginRequest dto) {

        var authPasswordToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = authenticationManager.authenticate(authPasswordToken);

        String token = tokenService.generateToken(auth.getName());

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("error.auth.user_not_found", dto.email()));

        return new AuthResult(token, userMapper.toResponseDTO(user));
    }
}
