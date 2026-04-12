package br.com.grifo.modules.catalog.dtos;

import java.util.List;
import java.util.UUID;

public record GenreResponseDTO(
        UUID id,
        List<GenreTranslationDTO> translations
) {}
