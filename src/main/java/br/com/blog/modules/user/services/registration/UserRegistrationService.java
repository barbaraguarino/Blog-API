package br.com.blog.modules.user.services.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public User registerUser(RegisterUserRequest dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new ResourceAlreadyExistsException("error.user.already_exists");
        }

        User newUser = User.createLocalUser(
                dto.name(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                generateRandomNickname(dto.name())
        );

        return userRepository.save(newUser);
    }

    public User registerWithGoogle(GoogleAuthRequest dto) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(dto.token());

            if (idToken == null) {
                throw new BadCredentialsException("error.auth.google_token_invalid");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            if (userRepository.existsByEmail(email))
                throw new ResourceAlreadyExistsException("error.user.already_exists");

            String googleId = payload.getSubject();

            if(userRepository.existsByGoogleId(googleId))
                throw new ResourceAlreadyExistsException("error.user.provider_conflict");

            String name = payload.get("name").toString();

            var newUser = User.createGoogleUser(
                    name,
                    email,
                    googleId,
                    generateRandomNickname(name)
            );

            return userRepository.save(newUser);

        } catch (Exception e) {
            if (e instanceof ResourceAlreadyExistsException) throw (ResourceAlreadyExistsException) e;
            throw new BadCredentialsException("error.auth.google_token_invalid");
        }
    }

    private String generateRandomNickname(String name) {
        String cleanName = name.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        if (cleanName.length() > 40) {
            cleanName = cleanName.substring(0, 40);
        }

        SecureRandom random = new SecureRandom();
        int suffix = 1000 + random.nextInt(9000);

        return cleanName + "_" + suffix;
    }
}
