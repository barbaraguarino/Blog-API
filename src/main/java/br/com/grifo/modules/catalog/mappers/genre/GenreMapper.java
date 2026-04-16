package br.com.grifo.modules.catalog.mappers.genre;

import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.dtos.genre.GenreResponseDTO;
import br.com.grifo.modules.catalog.dtos.genre.GenreTranslationDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GenreMapper {

    public GenreResponseDTO toResponseDTO(Genre genre) {

        List<GenreTranslationDTO> translationDTOs = genre.getTranslations().stream()
                .map(t -> new GenreTranslationDTO(t.getLanguageCode(), t.getName()))
                .toList();

        UUID parentId = (genre.getParent() != null) ? genre.getParent().getId() : null;

        return new GenreResponseDTO(
                genre.getId(),
                parentId,
                translationDTOs
        );
    }
}
