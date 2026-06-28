package br.com.blog.modules.user.dtos.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank(message = "error.user.name.not_blank")
        @Size(max = 150, message = "error.user.name.size")
        String name,

        @Email(message = "error.user.email.invalid")
        @NotBlank(message = "error.user.email.not_blank")
        @Size(max = 150, message = "error.user.email.size")
        String email,

        @NotBlank(message = "error.user.password.not_blank")
        @Size(min = 8, message = "error.user.password.size")
        @Pattern(
                regexp = "^(?=(?:.*[A-Z]){2})(?=(?:.*[a-z]){2})(?=(?:.*\\d){2})(?=.*[\\W_]).*$",
                message = "error.user.password.complexity"
        )
        String password
) {}
