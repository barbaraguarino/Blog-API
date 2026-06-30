package br.com.blog.modules.user.services.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.core.security.GoogleAuthGateway;
import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.auth.GoogleAuthRequest;
import br.com.blog.modules.user.dtos.registration.RegisterUserRequest;
import br.com.blog.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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
    private final GoogleAuthGateway googleAuthGateway;

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
        var googleUser = googleAuthGateway.extractUserInfo(dto.token());

        if (userRepository.existsByEmail(googleUser.email())) {
            throw new ResourceAlreadyExistsException("error.user.already_exists");
        }

        if (userRepository.existsByGoogleId(googleUser.googleId())) {
            throw new ResourceAlreadyExistsException("error.user.provider_conflict");
        }

        var newUser = User.createGoogleUser(
                googleUser.name(),
                googleUser.email(),
                googleUser.googleId(),
                generateRandomNickname(googleUser.name())
        );

        return userRepository.save(newUser);
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
