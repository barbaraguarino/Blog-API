package br.com.blog.modules.user.dtos.auth;

import br.com.blog.modules.user.dtos.shared.UserProfileResponseDTO;

public record AuthResultDTO(
        String token,
        UserProfileResponseDTO userProfile
){}
