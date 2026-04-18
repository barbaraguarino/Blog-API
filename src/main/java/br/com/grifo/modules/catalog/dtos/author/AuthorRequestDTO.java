package br.com.grifo.modules.catalog.dtos.author;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AuthorRequestDTO(
        @NotBlank(message = "error.validation.author_display_name.not_blank")
        @Size(max = 150, message = "error.validation.author_display_name.size")
        String displayName,

        @NotBlank(message = "error.validation.author_sort_name.not_blank")
        @Size(max = 150, message = "error.validation.author_sort_name.size")
        String sortName,

        LocalDate birthDate,
        String website,

        @NotEmpty(message = "error.validation.author.localizations.not_empty")
        @Valid List<AuthorLocalizationDTO> localizations
) {}
