package br.com.blog.modules.user.application.mappers;

import br.com.blog.modules.user.domain.models.User;
import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponseDTO toResponseDTO(User entity) {
        if (entity == null) {
            return null;
        }

        return new UserProfileResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getRole().name(),
                entity.isLinkedToGoogle(),
                entity.isEnabled(),
                entity.isLocked(),
                entity.getCreatedAt()
        );
    }
}
