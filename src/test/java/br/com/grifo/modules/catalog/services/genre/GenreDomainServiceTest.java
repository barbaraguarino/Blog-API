package br.com.grifo.modules.catalog.services.genre;

import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.domain.genre.GenreTranslation;
import br.com.grifo.modules.catalog.dtos.genre.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.genre.GenreTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreDomainServiceTest {

    @Mock
    private GenreTranslationRepository translationRepository;

    @InjectMocks
    private GenreDomainService genreDomainService;

    private Genre genre;
    private GenreTranslationDTO defaultPtBrDto;
    private GenreTranslation existingPtBrTranslation;

    @BeforeEach
    void setUp() {
        genre = Genre.createGenre();

        defaultPtBrDto = new GenreTranslationDTO("pt-BR", "Fantasia");
        existingPtBrTranslation = GenreTranslation.create("pt-BR", "Fantasia");
    }

    @Test
    @DisplayName("Deve adicionar todas as traduções quando nenhuma existir no banco de dados")
    void shouldAddAllTranslationsWhenNoneExist() {
        List<GenreTranslationDTO> translationDTOList = List.of(
                defaultPtBrDto,
                new GenreTranslationDTO("en-US", "Fantasy")
        );

        when(translationRepository.findByLanguageCodeInAndNameInIgnoreCase(anyList(), anyList()))
                .thenReturn(List.of());

        genreDomainService.addOnlyNewTranslations(genre, translationDTOList);

        assertEquals(2, genre.getTranslations().size(), "Deve ter adicionado as duas traduções inéditas");
    }

    @Test
    @DisplayName("Deve ignorar traduções repetidas e adicionar apenas as inéditas")
    void shouldFilterExistingTranslationsAndAddOnlyNewOnes() {
        List<GenreTranslationDTO> translationDTOList = List.of(
                defaultPtBrDto,
                new GenreTranslationDTO("es-ES", "Fantasía")
        );

        when(translationRepository.findByLanguageCodeInAndNameInIgnoreCase(anyList(), anyList()))
                .thenReturn(List.of(existingPtBrTranslation));

        genreDomainService.addOnlyNewTranslations(genre, translationDTOList);

        assertEquals(1, genre.getTranslations().size(), "Deve ter filtrado a repetida e adicionado apenas uma");

        boolean hasSpanish = genre.getTranslations().stream()
                .anyMatch(t -> t.getLanguageCode().equals("es-ES"));
        assertTrue(hasSpanish);
    }

    @Test
    @DisplayName("Não deve adicionar nada se todas as traduções enviadas já existirem no banco")
    void shouldAddNothingWhenAllTranslationsExist() {
        List<GenreTranslationDTO> translationDTOList = List.of(defaultPtBrDto);

        when(translationRepository.findByLanguageCodeInAndNameInIgnoreCase(anyList(), anyList()))
                .thenReturn(List.of(existingPtBrTranslation));

        genreDomainService.addOnlyNewTranslations(genre, translationDTOList);

        assertTrue(genre.getTranslations().isEmpty(), "A lista do gênero deve continuar vazia");
    }
}