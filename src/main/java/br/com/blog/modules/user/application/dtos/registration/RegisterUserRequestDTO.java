package br.com.blog.modules.user.application.dtos.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequestDTO(
        @NotBlank(message = "error.validation.user.name.not_blank")
        @Size(max = 150, message = "error.validation.user.name.size")
        String name,

        @Email(message = "error.validation.user.email.invalid")
        @NotBlank(message = "error.validation.user.email.not_blank")
        @Size(max = 150, message = "error.validation.user.email.size")
        String email,

        @NotBlank(message = "error.validation.user.password.not_blank")
        @Size(min = 8, message = "error.validation.user.password.size")
        @Pattern(regexp = "^(?=(?:.*[A-Z]){2}).*$", message = "error.validation.user.password.uppercase")
        @Pattern(regexp = "^(?=(?:.*[a-z]){2}).*$", message = "error.validation.user.password.lowercase")
        @Pattern(regexp = "^(?=(?:.*\\d){2}).*$", message = "error.validation.user.password.number")
        @Pattern(regexp = "^(?=.*[\\W_]).*$", message = "error.validation.user.password.special")
        String password
) {}
