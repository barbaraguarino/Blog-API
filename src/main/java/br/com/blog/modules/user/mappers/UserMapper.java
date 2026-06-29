package br.com.blog.modules.user.mappers;

import br.com.blog.modules.user.domain.User;
import br.com.blog.modules.user.dtos.shared.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toResponseDTO(User entity) {
        if (entity == null) {
            return null;
        }

        return new UserProfileResponse(
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
