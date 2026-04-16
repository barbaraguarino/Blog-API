package br.com.grifo.modules.user.dtos.shared;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
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
