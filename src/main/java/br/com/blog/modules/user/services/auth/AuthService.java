package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.BusinessException;
import br.com.blog.core.security.JwtTokenProvider;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleTokenDTO;
import br.com.blog.modules.user.dtos.auth.LoginRequestDTO;
import br.com.blog.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public record AuthResult(String token, User user) {}

    public AuthResult authenticate(LoginRequestDTO dto) {

        var authPasswordToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = authenticationManager.authenticate(authPasswordToken);

        String token = jwtTokenProvider.generateToken(auth.getName());

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new BusinessException("error.auth.user_not_found", HttpStatus.NOT_FOUND));

        return new AuthResult(token, user);
    }

    public AuthResult authenticateWithGoogle(GoogleTokenDTO dto){
        try {

            GoogleIdToken idToken = googleIdTokenVerifier.verify(dto.token());

            if (idToken == null) {
                throw new BusinessException("error.auth.invalid_google_token", HttpStatus.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleSubjectId = payload.getSubject();

            User user = userRepository.findByGoogleId(googleSubjectId)
                    .orElseThrow(() -> new BusinessException("error.auth.user_not_found_google", HttpStatus.NOT_FOUND));

            String token = jwtTokenProvider.generateToken(user.getEmail());

            return new AuthResult(token, user);
        }catch (Exception e) {

            if (e instanceof BusinessException) throw (BusinessException) e;
            throw new BusinessException("error.auth.invalid_google_token", HttpStatus.UNAUTHORIZED);
        }
    }

}
