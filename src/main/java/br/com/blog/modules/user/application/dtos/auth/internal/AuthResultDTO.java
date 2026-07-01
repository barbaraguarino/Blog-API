package br.com.blog.modules.user.application.dtos.auth.internal;

import br.com.blog.modules.user.application.dtos.shared.UserProfileResponseDTO;

public record AuthResultDTO(
        String token,
        UserProfileResponseDTO userProfile
){}
