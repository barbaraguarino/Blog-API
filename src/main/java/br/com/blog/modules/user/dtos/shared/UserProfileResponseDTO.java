package br.com.blog.modules.user.dtos.shared;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponseDTO(
        UUID id,
        String name,
        String email,
        String nickname,
        String role,
        boolean isLinkedToGoogle,
        boolean enabled,
        boolean locked,
        LocalDateTime createdAt
) {}
