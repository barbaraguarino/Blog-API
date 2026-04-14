package br.com.grifo.modules.catalog.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.domain.GenreTranslation;
import br.com.grifo.modules.catalog.dtos.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.GenreRepository;
import br.com.grifo.modules.catalog.repositories.GenreTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreTranslationRepository translationRepository;

    public Genre createGenre(GenreRequestDTO dto) {
        for (GenreTranslationDTO translationDto : dto.translations())
            if (translationRepository.existsByLanguageCodeAndNameIgnoreCase(
                    translationDto.languageCode(), translationDto.name())) {
                throw new BusinessException("error.catalog.genre.name_already_exists", HttpStatus.CONFLICT);
            }

        Genre genre = new Genre();

        dto.translations().forEach(translationDTO -> {
            GenreTranslation translation = new GenreTranslation();
            translation.setLanguageCode(translationDTO.languageCode());
            translation.setName(translationDTO.name());
            genre.addTranslation(translation);
        });

        return genreRepository.save(genre);
    }

    public Genre createSubgenre(UUID parentId, GenreRequestDTO dto) {
        Genre parentGenre = genreRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("error.catalog.genre.not_found", HttpStatus.NOT_FOUND));

        Genre subgenre = new Genre();
        subgenre.setParent(parentGenre);

        dto.translations().forEach(translationDTO -> {
            GenreTranslation translation = new GenreTranslation();
            translation.setLanguageCode(translationDTO.languageCode());
            translation.setName(translationDTO.name());
            subgenre.addTranslation(translation);
        });

        return genreRepository.save(subgenre);
    }
}
