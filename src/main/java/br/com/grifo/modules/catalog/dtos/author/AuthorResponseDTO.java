package br.com.grifo.modules.catalog.dtos.author;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AuthorResponseDTO(
        UUID id,
        String displayName,
        String sortName,
        LocalDate birthDate,
        String website,
        List<AuthorLocalizationDTO> localizations
) {}
