package br.com.blog.modules.user.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequestDTO(
        @NotBlank(message = "error.validation.auth.google_token_required")
        String token
) {}
