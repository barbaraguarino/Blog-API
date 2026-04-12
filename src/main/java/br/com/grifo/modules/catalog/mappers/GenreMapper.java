package br.com.grifo.modules.catalog.mappers;

import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.dtos.GenreResponseDTO;
import br.com.grifo.modules.catalog.dtos.GenreTranslationDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenreMapper {

    public GenreResponseDTO toResponseDTO(Genre genre) {

        List<GenreTranslationDTO> translationDTOs = genre.getTranslations().stream()
                .map(t -> new GenreTranslationDTO(t.getLanguageCode(), t.getName()))
                .toList();

        return new GenreResponseDTO(
                genre.getId(),
                translationDTOs
        );
    }
}
