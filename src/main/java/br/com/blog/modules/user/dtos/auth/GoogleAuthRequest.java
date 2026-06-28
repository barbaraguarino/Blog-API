package br.com.blog.modules.user.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "error.auth.google_token_required")
        String token
) {}
