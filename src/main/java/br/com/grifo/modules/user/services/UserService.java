package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponseDTO registerUser(UserRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);
        }

        User newUser = userMapper.toEntity(dto);

        newUser.setRole(UserRole.READER);
        newUser.setUsername(generateRandomUsername(newUser.getName()));
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User savedUser = userRepository.save(newUser);
        return userMapper.toResponseDTO(savedUser);
    }

    private String generateRandomUsername(String name) {
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
