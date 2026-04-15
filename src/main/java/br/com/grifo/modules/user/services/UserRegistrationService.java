package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    public User registerUser(UserRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);
        }

        User newUser = User.createLocalUser(
                dto.name(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                generateRandomNickname(dto.name())
        );

        return userRepository.save(newUser);
    }

    public User registerWithGoogle(GoogleTokenDTO dto) {
        try {

            GoogleIdToken idToken = googleIdTokenVerifier.verify(dto.token());

            if (idToken == null) {
                throw new BusinessException("error.auth.invalid_google_token", HttpStatus.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();

            if (userRepository.existsByEmail(email))
                throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);

            String googleId = payload.getSubject();

            if(userRepository.existsByGoogleId(googleId))
                throw new BusinessException("error.user.provider_conflict", HttpStatus.CONFLICT);

            String name = payload.get("name").toString();

            var newUser = User.createGoogleUser(
                    name,
                    email,
                    googleId,
                    generateRandomNickname(name)
            );

            return userRepository.save(newUser);

        } catch (Exception e) {

            if (e instanceof BusinessException) throw (BusinessException) e;
            throw new BusinessException("error.auth.invalid_google_token", HttpStatus.UNAUTHORIZED);
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
