package br.com.grifo.modules.catalog.services.author;

import br.com.grifo.modules.catalog.domain.author.Author;
import br.com.grifo.modules.catalog.dtos.author.AuthorLocalizationDTO;
import br.com.grifo.modules.catalog.dtos.author.AuthorRequestDTO;
import br.com.grifo.modules.catalog.repositories.author.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Captor
    private ArgumentCaptor<Author> authorCaptor;

    @Nested
    @DisplayName("Criação de Autor")
    class CreateAuthor {

        private AuthorRequestDTO requestDTO;

        @BeforeEach
        void setUp() {
            List<AuthorLocalizationDTO> localizations = List.of(
                    new AuthorLocalizationDTO("pt-BR", "J.R.R. Tolkien foi um escritor..."),
                    new AuthorLocalizationDTO("en-US", "J.R.R. Tolkien was an English writer...")
            );

            requestDTO = new AuthorRequestDTO(
                    "J.R.R. Tolkien",
                    "Tolkien, J.R.R.",
                    LocalDate.of(1892, 1, 3),
                    "https://www.tolkienestate.com",
                    localizations
            );
        }

        @Test
        @DisplayName("Deve criar e salvar um autor com as suas biografias com sucesso")
        void shouldCreateAuthorSuccessfully() {
            Author savedAuthorMock = Author.create(
                    requestDTO.displayName(),
                    requestDTO.sortName(),
                    requestDTO.birthDate(),
                    requestDTO.website()
            );
            ReflectionTestUtils.setField(savedAuthorMock, "id", UUID.randomUUID());

            when(authorRepository.save(any(Author.class))).thenReturn(savedAuthorMock);

            Author result = authorService.createAuthor(requestDTO);

            assertNotNull(result);
            assertEquals(savedAuthorMock.getId(), result.getId());

            verify(authorRepository, times(1)).save(authorCaptor.capture());
            Author capturedAuthor = authorCaptor.getValue();

            assertEquals("J.R.R. Tolkien", capturedAuthor.getDisplayName());
            assertEquals("Tolkien, J.R.R.", capturedAuthor.getSortName());
            assertEquals(LocalDate.of(1892, 1, 3), capturedAuthor.getBirthDate());
            assertEquals("https://www.tolkienestate.com", capturedAuthor.getWebsite());

            assertEquals(2, capturedAuthor.getLocalizations().size(), "Deve conter exatamente 2 biografias cadastradas");

            boolean allLocalizationsHaveAuthor = capturedAuthor.getLocalizations().stream()
                    .allMatch(loc -> loc.getAuthor() != null && loc.getAuthor().equals(capturedAuthor));

            assertTrue(allLocalizationsHaveAuthor, "Todas as biografias devem estar apontando para o autor recém-criado");
        }
    }
}