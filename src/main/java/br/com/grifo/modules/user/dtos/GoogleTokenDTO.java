package br.com.grifo.modules.user.dtos;

import jakarta.validation.constraints.NotBlank;

public record GoogleTokenDTO(
        @NotBlank(message = "error.auth.google_token_required")
        String token
) {}
