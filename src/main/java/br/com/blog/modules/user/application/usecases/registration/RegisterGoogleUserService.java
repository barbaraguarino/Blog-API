package br.com.blog.modules.user.application.usecases.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.infrastructure.providers.google.GoogleAuthGateway;
import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.auth.GoogleAuthRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.mappers.UserMapper;
import br.com.blog.modules.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterGoogleUserService {

    private final GoogleAuthGateway googleAuthGateway;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserProfileResponseDTO execute(GoogleAuthRequestDTO dto) {

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

        User savedUser = userRepository.save(newUser);

        return userMapper.toResponseDTO(savedUser);
    }
}
