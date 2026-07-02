package br.com.blog.modules.user.application.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "error.validation.user.login.not_blank")
        String login,

        @NotBlank(message = "error.validation.user.password.not_blank")
        String password
) {}
