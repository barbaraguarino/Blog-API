package br.com.blog.modules.user.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "error.validation.user.email.not_blank")
        @Email(message = "error.validation.user.email.invalid")
        String email,

        @NotBlank(message = "error.validation.user.password.not_blank")
        String password
) {}
