package br.com.grifo.modules.catalog.services.genre;

import br.com.grifo.core.exceptions.BusinessException;
import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.domain.genre.GenreTranslation;
import br.com.grifo.modules.catalog.dtos.genre.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.genre.GenreTranslationDTO;
import br.com.grifo.modules.catalog.repositories.genre.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreDomainService genreDomainService;

    @InjectMocks
    private GenreService genreService;

    @Nested
    @DisplayName("Criação de Gênero Raiz")
    class CreateGenre {

        private GenreRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            requestDTO = new GenreRequestDTO(List.of(new GenreTranslationDTO("pt-BR", "Fantasia")));
        }

        @Test
        @DisplayName("Deve salvar o gênero quando o Domain Service adicionar pelo menos uma tradução inédita")
        void shouldReturnGenreWhenTranslationsAreAdded() {
            var savedGenreMock = Genre.createGenre();
            ReflectionTestUtils.setField(savedGenreMock, "id", UUID.randomUUID());

            doAnswer(invocation -> {
                Genre genreArgument = invocation.getArgument(0);
                genreArgument.addTranslation(GenreTranslation.create("pt-BR", "Fantasia"));
                return null;
            }).when(genreDomainService).addOnlyNewTranslations(any(Genre.class), anyList());

            when(genreRepository.save(any(Genre.class))).thenReturn(savedGenreMock);

            Genre result = genreService.createGenre(requestDTO);

            assertNotNull(result);
            assertEquals(savedGenreMock.getId(), result.getId());
            verify(genreRepository, times(1)).save(any(Genre.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando todas as traduções enviadas já existirem")
        void shouldThrowBusinessExceptionWhenAllTranslationsExist() {
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createGenre(requestDTO)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
            assertEquals("error.catalog.genre.all_translations_exist", exception.getMessageKey());
            verify(genreRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Criação de Subgêneros")
    class CreateSubgenre {

        private UUID parentId;
        private GenreRequestDTO requestDTO;
        private Genre parentGenre;

        @BeforeEach
        void setUp() {
            parentId = UUID.randomUUID();
            parentGenre = Genre.createGenre();
            ReflectionTestUtils.setField(parentGenre, "id", parentId);

            requestDTO = new GenreRequestDTO(List.of(new GenreTranslationDTO("pt-BR", "Alta Fantasia")));

        }

        @Test
        @DisplayName("Deve salvar o subgênero com sucesso quando o pai existir e houver traduções inéditas")
        void shouldReturnSubgenreWhenParentExistsAndTranslationsAreAdded() {
            var savedSubgenreMock = Genre.createSubgenre(parentGenre);
            ReflectionTestUtils.setField(savedSubgenreMock, "id", UUID.randomUUID());

            when(genreRepository.findById(parentId)).thenReturn(Optional.of(parentGenre));

            doAnswer(invocation -> {
                Genre genreArgument = invocation.getArgument(0);
                genreArgument.addTranslation(GenreTranslation.create("pt-BR", "Alta Fantasia"));
                return null;
            }).when(genreDomainService).addOnlyNewTranslations(any(Genre.class), anyList());

            when(genreRepository.save(any(Genre.class))).thenReturn(savedSubgenreMock);

            Genre result = genreService.createSubgenre(parentId, requestDTO);

            assertNotNull(result);
            assertEquals(savedSubgenreMock.getId(), result.getId());
            assertNotNull(result.getParent());
            verify(genreRepository, times(1)).save(any(Genre.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException (409) quando todas as traduções do subgênero já existirem")
        void shouldThrowBusinessExceptionWhenAllTranslationsExist() {
            when(genreRepository.findById(parentId)).thenReturn(Optional.of(parentGenre));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createSubgenre(parentId, requestDTO)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
            assertEquals("error.catalog.genre.all_translations_exist", exception.getMessageKey());
            verify(genreRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException (404) quando o gênero pai não for encontrado")
        void shouldThrowBusinessExceptionWhenParentNotFound() {
            when(genreRepository.findById(parentId)).thenReturn(Optional.empty());

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    genreService.createSubgenre(parentId, requestDTO)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
            assertEquals("error.catalog.genre.not_found", exception.getMessageKey());

            verify(genreDomainService, never()).addOnlyNewTranslations(any(), any());
            verify(genreRepository, never()).save(any());
        }
    }
}