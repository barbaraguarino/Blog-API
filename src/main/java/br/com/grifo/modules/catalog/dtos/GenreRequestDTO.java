package br.com.grifo.modules.catalog.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GenreRequestDTO(
        @NotEmpty(message = "error.validation.genre.translations.not_empty")
        @Valid List<GenreTranslationDTO> translations
) {}
