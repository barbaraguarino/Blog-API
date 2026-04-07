package br.com.grifo.modules.user.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.domain.enums.UserRole;
import br.com.grifo.modules.user.dtos.GoogleTokenDTO;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import br.com.grifo.modules.user.mappers.UserMapper;
import br.com.grifo.modules.user.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Value("${api.security.google.client-id}")
    private String googleClientId;

    public UserResponseDTO registerUser(UserRegistrationDTO dto) {

        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);
        }

        User newUser = userMapper.toEntity(dto);

        newUser.setRole(UserRole.READER);
        newUser.setNickname(generateRandomNickname(newUser.getName()));
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User savedUser = userRepository.save(newUser);
        return userMapper.toResponseDTO(savedUser);
    }

    public UserResponseDTO registerWithGoogle(GoogleTokenDTO dto) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(dto.token());

            if (idToken == null) {
                throw new BusinessException("error.auth.invalid_google_token", HttpStatus.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String googleSubjectId = payload.getSubject();

            Optional<User> existingUserOpt = userRepository.findByEmail(email);

            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (existingUser.getGoogleId() == null) {
                    throw new BusinessException("error.user.provider_conflict", HttpStatus.CONFLICT);
                }
                throw new BusinessException("error.user.already_exists", HttpStatus.CONFLICT);
            }

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setGoogleId(googleSubjectId);
            newUser.setRole(UserRole.READER);
            newUser.setNickname(generateRandomNickname(name));

            User savedUser = userRepository.save(newUser);
            return userMapper.toResponseDTO(savedUser);

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
