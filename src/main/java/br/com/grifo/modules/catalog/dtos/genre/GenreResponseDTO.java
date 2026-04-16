package br.com.grifo.modules.catalog.dtos.genre;

import java.util.List;
import java.util.UUID;

public record GenreResponseDTO(
        UUID id,
        UUID parentId,
        List<GenreTranslationDTO> translations
) {}
