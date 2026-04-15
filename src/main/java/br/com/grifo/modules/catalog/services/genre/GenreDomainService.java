package br.com.grifo.modules.catalog.services.genre;

import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.domain.genre.GenreTranslation;
import br.com.grifo.modules.catalog.dtos.genre.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.genre.GenreTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class GenreDomainService {

    private final GenreTranslationRepository translationRepository;

    public void addOnlyNewTranslations(Genre genre, List<GenreTranslationDTO> genreTranslationDTOS) {

        List<String> langs = genreTranslationDTOS.stream().map(GenreTranslationDTO::languageCode).toList();
        List<String> names = genreTranslationDTOS.stream().map(GenreTranslationDTO::name).toList();

        List<GenreTranslation> existing = translationRepository
                .findByLanguageCodeInAndNameInIgnoreCase(langs, names);

        Set<String> existingPairs = existing.stream()
                .map(t -> t.getLanguageCode().toLowerCase() + ":" + t.getName().toLowerCase())
                .collect(Collectors.toSet());

        genreTranslationDTOS.stream()
                .filter(dto -> {
                    String key = dto.languageCode().toLowerCase() + ":" + dto.name().toLowerCase();
                    return !existingPairs.contains(key);
                })
                .forEach(dto -> {
                    GenreTranslation translation = GenreTranslation.create(
                            dto.languageCode(),
                            dto.name()
                    );
                    genre.addTranslation(translation);
                });
    }
}