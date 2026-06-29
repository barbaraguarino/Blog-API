package br.com.blog.modules.user.services.auth;

import br.com.blog.core.exceptions.domain.ResourceNotFoundException;
import br.com.blog.core.security.TokenService;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.auth.LoginRequest;
import br.com.blog.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public record AuthResult(String token, User user) {}

    public AuthResult authenticate(LoginRequest dto) {

        var authPasswordToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = authenticationManager.authenticate(authPasswordToken);

        String token = tokenService.generateToken(auth.getName());

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResourceNotFoundException("error.auth.user_not_found", dto.email()));

        return new AuthResult(token, user);
    }

    public AuthResult authenticateWithGoogle(GoogleAuthRequest dto){
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(dto.token());

            if (idToken == null) {
                throw new BadCredentialsException("error.auth.google_token_invalid");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleSubjectId = payload.getSubject();

            User user = userRepository.findByGoogleId(googleSubjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("error.auth.user_not_found_google"));

            String token = tokenService.generateToken(user.getEmail());

            return new AuthResult(token, user);

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) throw (ResourceNotFoundException) e;
            throw new BadCredentialsException("error.auth.google_token_invalid");
        }
    }
}