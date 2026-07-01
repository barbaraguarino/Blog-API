package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.GoogleAuthGateway;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.AuthResultDTO;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequestDTO;
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
    public AuthResultDTO execute(GoogleAuthRequestDTO dto) {

        var googleUser = googleAuthGateway.extractUserInfo(dto.token());

        User user = userRepository.findByGoogleId(googleUser.googleId())
                .orElseThrow(() -> new ResourceNotFoundException("error.auth.user_not_found_google"));

        String token = tokenService.generateToken(user.getEmail());

        return new AuthResultDTO(token, userMapper.toResponseDTO(user));
    }
}
