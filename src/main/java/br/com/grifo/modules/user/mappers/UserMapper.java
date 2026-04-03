package br.com.grifo.modules.user.mappers;

import br.com.grifo.modules.user.domain.User;
import br.com.grifo.modules.user.dtos.UserRegistrationDTO;
import br.com.grifo.modules.user.dtos.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRegistrationDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());

        user.setPassword(dto.password());

        return user;
    }

    public UserResponseDTO toResponseDTO(User entity) {
        if (entity == null) {
            return null;
        }

        return new UserResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getRole().name(),
                entity.isEnabled(),
                entity.isLocked(),
                entity.getCreatedAt()
        );
    }
}
