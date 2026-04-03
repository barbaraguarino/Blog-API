package br.com.grifo.modules.user.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String username,
        String role,
        boolean enabled,
        boolean locked,
        LocalDateTime createdAt
) {}
