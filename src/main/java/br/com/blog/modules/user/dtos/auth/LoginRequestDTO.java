package br.com.blog.modules.user.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "error.user.email.not_blank")
        @Email(message = "error.user.email.invalid")
        String email,

        @NotBlank(message = "error.user.password.not_blank")
        String password
) {}
