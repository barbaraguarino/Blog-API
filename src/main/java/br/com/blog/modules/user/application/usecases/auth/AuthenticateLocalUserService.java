package br.com.blog.modules.user.application.usecases.auth;

import br.com.blog.core.security.jwt.TokenService;
import br.com.blog.core.security.userdetails.CustomUserDetails;
import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.auth.internal.AuthResultDTO;
import br.com.blog.modules.user.application.dtos.auth.LoginRequestDTO;
import br.com.blog.modules.user.application.mappers.UserMapper;
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
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public AuthResultDTO execute(LoginRequestDTO dto) {

        var authPasswordToken = new UsernamePasswordAuthenticationToken(dto.login(), dto.password());
        var auth = authenticationManager.authenticate(authPasswordToken);

        var customUserDetails = (CustomUserDetails) auth.getPrincipal();
        assert customUserDetails != null;
        User user = customUserDetails.user();

        String token = tokenService.generateToken(user.getEmail());
        return new AuthResultDTO(token, userMapper.toResponseDTO(user));
    }
}
