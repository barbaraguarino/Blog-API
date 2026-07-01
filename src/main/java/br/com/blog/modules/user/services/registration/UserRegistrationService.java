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
                passwordEncoder.encode(dto.password())
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
                googleUser.googleId()
        );

        return userRepository.save(newUser);
    }
}
