package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.GoogleAuthGateway;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.AuthResult;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.mappers.UserMapper;
import br.com.blog.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticateGoogleUserService {

    private final GoogleAuthGateway googleAuthGateway;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AuthResult execute(GoogleAuthRequest dto) {

        var googleUser = googleAuthGateway.extractUserInfo(dto.token());

        User user = userRepository.findByGoogleId(googleUser.googleId())
                .orElseThrow(() -> new ResourceNotFoundException("error.auth.user_not_found_google"));

        String token = tokenService.generateToken(user.getEmail());

        return new AuthResult(token, userMapper.toResponseDTO(user));
    }
}
