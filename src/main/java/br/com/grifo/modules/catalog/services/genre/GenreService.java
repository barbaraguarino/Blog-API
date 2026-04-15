package br.com.grifo.modules.catalog.services.genre;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.dtos.genre.GenreRequestDTO;
import br.com.grifo.modules.catalog.repositories.genre.GenreRepository;
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
    private final GenreDomainService genreDomainService;

    public Genre createGenre(GenreRequestDTO dto) {
        var genre = Genre.createGenre();

        genreDomainService.addOnlyNewTranslations(genre, dto.translations());

        if (genre.getTranslations().isEmpty()) {
            throw new BusinessException("error.catalog.genre.all_translations_exist", HttpStatus.CONFLICT);
        }

        return genreRepository.save(genre);
    }

    public Genre createSubgenre(UUID parentId, GenreRequestDTO dto) {
        Genre parentGenre = genreRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("error.catalog.genre.not_found", HttpStatus.NOT_FOUND));

        Genre subgenre = Genre.createSubgenre(parentGenre);
        genreDomainService.addOnlyNewTranslations(subgenre, dto.translations());

        if (subgenre.getTranslations().isEmpty()) {
            throw new BusinessException("error.catalog.genre.all_translations_exist", HttpStatus.CONFLICT);
        }

        return genreRepository.save(subgenre);
    }

}
