package br.com.grifo.modules.catalog.services;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.dtos.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.GenreRepository;
import br.com.grifo.modules.catalog.repositories.GenreTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("Criação de gênero")
    class CreateGenre {

        private GenreRequestDTO requestDTO;

        @BeforeEach
        void setUp() {

            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Fantasia");
            requestDTO = new GenreRequestDTO(List.of(translationDTO));
        }

        @Test
        @DisplayName("Deve criar gênero com sucesso ao enviar traduções inéditas")
        void shouldReturnGenre_WhenTranslationsAreUnique() {

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

            when(translationRepository.existsByLanguageCodeAndNameIgnoreCase("pt-BR", "Fantasia")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createGenre(requestDTO)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
            assertEquals("error.catalog.genre.name_already_exists", exception.getMessageKey());
            verify(genreRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Criação de subgêneros")
    class CreateSubgenre {

        private UUID parentId;
        private Genre parentGenre;
        private GenreRequestDTO requestDTO;

        @BeforeEach
        void setUp() {

            parentId = UUID.randomUUID();

            parentGenre = new Genre();
            parentGenre.setId(parentId);

            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Alta Fantasia");
            requestDTO = new GenreRequestDTO(List.of(translationDTO));
        }

        @Test
        @DisplayName("Deve criar subgênero com sucesso ao informar um pai válido e traduções inéditas")
        void shouldReturnSubgenre_WhenParentExistsAndTranslationsAreUnique() {

            Genre savedSubgenreMock = new Genre();
            savedSubgenreMock.setId(UUID.randomUUID());
            savedSubgenreMock.setParent(parentGenre);

            when(genreRepository.findById(parentId)).thenReturn(java.util.Optional.of(parentGenre));
            when(translationRepository.existsByLanguageCodeAndNameIgnoreCase("pt-BR", "Alta Fantasia")).thenReturn(false);
            when(genreRepository.save(any(Genre.class))).thenReturn(savedSubgenreMock);

            Genre result = genreService.createSubgenre(parentId, requestDTO);

            assertNotNull(result);
            assertEquals(savedSubgenreMock.getId(), result.getId());
            assertNotNull(result.getParent());
            assertEquals(parentId, result.getParent().getId());

            verify(genreRepository, times(1)).findById(parentId);
            verify(genreRepository, times(1)).save(any(Genre.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException (404) quando o gênero pai não for encontrado")
        void shouldThrowBusinessException_WhenParentNotFound() {

            when(genreRepository.findById(parentId)).thenReturn(java.util.Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createSubgenre(parentId, requestDTO)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("error.catalog.genre.not_found", exception.getMessageKey());

            verify(translationRepository, never()).existsByLanguageCodeAndNameIgnoreCase(any(), any());
            verify(genreRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException (409) ao enviar nome de subgênero já existente")
        void shouldThrowBusinessException_WhenSubgenreNameAlreadyExists() {

            when(genreRepository.findById(parentId)).thenReturn(java.util.Optional.of(parentGenre));
            when(translationRepository.existsByLanguageCodeAndNameIgnoreCase("pt-BR", "Alta Fantasia")).thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createSubgenre(parentId, requestDTO)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
            assertEquals("error.catalog.genre.name_already_exists", exception.getMessageKey());

            verify(genreRepository, times(1)).findById(parentId);
            verify(genreRepository, never()).save(any());
        }
    }

}
