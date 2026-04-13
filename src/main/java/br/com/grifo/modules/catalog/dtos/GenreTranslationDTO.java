package br.com.grifo.modules.catalog.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GenreTranslationDTO(

        @NotBlank(message = "{error.validation.language_code.not_blank}")
        @Pattern(regexp = "^[a-z]{2}-[A-Z]{2}$", message = "{error.validation.language_code.pattern}")
        String languageCode,

        @NotBlank(message = "{error.validation.genre_name.not_blank}")
        @Size(max = 150, message = "{error.validation.genre_name.size^}")
        String name
) {}
