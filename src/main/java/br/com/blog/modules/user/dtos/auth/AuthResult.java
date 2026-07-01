package br.com.blog.modules.user.dtos.auth;

import br.com.blog.modules.user.dtos.shared.UserProfileResponse;

public record AuthResult(
        String token,
        UserProfileResponse userProfile
){}
