package br.com.grifo.modules.catalog.dtos.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AuthorLocalizationDTO(
        @NotBlank(message = "error.validation.language_code.not_blank")
        @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "error.validation.language_code.pattern")
        String languageCode,

        @NotBlank(message = "error.validation.author_biography.not_blank")
        String biography
) {}
