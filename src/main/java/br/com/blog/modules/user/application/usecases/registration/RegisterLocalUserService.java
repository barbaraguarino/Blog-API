package br.com.blog.modules.user.application.usecases.registration;

import br.com.blog.core.exceptions.domain.ResourceAlreadyExistsException;
import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.registration.RegisterUserRequestDTO;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import br.com.blog.modules.user.application.mappers.UserMapper;
import br.com.blog.modules.user.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterLocalUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserProfileResponseDTO execute(RegisterUserRequestDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new ResourceAlreadyExistsException("error.user.already_exists");
        }

        User newUser = User.createLocalUser(
                dto.name(),
                dto.email(),
                passwordEncoder.encode(dto.password())
        );

        User savedUser = userRepository.save(newUser);

        return userMapper.toResponseDTO(savedUser);
    }
}
