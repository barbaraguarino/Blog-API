package br.com.grifo.modules.catalog.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.dtos.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.GenreRepository;
import br.com.grifo.modules.catalog.repositories.GenreTranslationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreTranslationRepository translationRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    @DisplayName("Deve criar gênero com sucesso ao enviar traduções inéditas")
    void shouldReturnGenre_WhenTranslationsAreUnique() {
        GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Fantasia");
        GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

        Genre savedGenreMock = new Genre();
        savedGenreMock.setId(UUID.randomUUID());

        when(translationRepository.existsByLanguageCodeAndNameIgnoreCase("pt-BR", "Fantasia")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenReturn(savedGenreMock);

        Genre result = genreService.createGenre(requestDTO);

        assertNotNull(result);
        assertEquals(savedGenreMock.getId(), result.getId());
        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao enviar nome de gênero já existente")
    void shouldThrowBusinessException_WhenNameAlreadyExists() {
        GenreTranslationDTO translationDTO = new GenreTranslationDTO("en-US", "Fantasy");
        GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

        when(translationRepository.existsByLanguageCodeAndNameIgnoreCase("en-US", "Fantasy")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
            genreService.createGenre(requestDTO)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals("error.catalog.genre.name_already_exists", exception.getMessageKey());
        verify(genreRepository, never()).save(any());
    }
}
