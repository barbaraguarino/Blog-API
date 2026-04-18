package br.com.grifo.modules.catalog.controllers.author;

import br.com.grifo.core.exceptions.GlobalExceptionHandler;
import br.com.grifo.core.security.CustomUserDetailsService;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.catalog.domain.author.Author;
import br.com.grifo.modules.catalog.dtos.author.AuthorLocalizationDTO;
import br.com.grifo.modules.catalog.dtos.author.AuthorRequestDTO;
import br.com.grifo.modules.catalog.dtos.author.AuthorResponseDTO;
import br.com.grifo.modules.catalog.mappers.author.AuthorMapper;
import br.com.grifo.modules.catalog.services.author.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private AuthorMapper authorMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("Cria novos autores")
    class CreateAuthor {

        private AuthorRequestDTO validRequestDTO;
        private AuthorResponseDTO responseDTO;
        private Author fakeAuthor;

        @BeforeEach
        void setUp() {
            List<AuthorLocalizationDTO> localizations = List.of(
                    new AuthorLocalizationDTO("pt-BR", "Criador de O Senhor dos Anéis")
            );

            validRequestDTO = new AuthorRequestDTO(
                    "J.R.R. Tolkien",
                    "Tolkien, J.R.R.",
                    null,
                    "https://www.tolkienestate.com",
                    localizations
            );

            fakeAuthor = Author.create(
                    "J.R.R. Tolkien",
                    "Tolkien, J.R.R.",
                    LocalDate.of(1892, 1, 3),
                    "https://www.tolkienestate.com"
            );
            ReflectionTestUtils.setField(fakeAuthor, "id", UUID.randomUUID());

            responseDTO = new AuthorResponseDTO(
                    fakeAuthor.getId(),
                    "J.R.R. Tolkien",
                    "Tolkien, J.R.R.",
                    LocalDate.of(1892, 1, 3),
                    "https://www.tolkienestate.com",
                    localizations
            );
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 201 Created quando o DTO for válido (Data Nula no Payload)")
        void shouldReturn201WhenValidRequest() throws Exception {

            when(authorService.createAuthor(any(AuthorRequestDTO.class))).thenReturn(fakeAuthor);
            when(authorMapper.toResponseDTO(any(Author.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/authors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.displayName").value("J.R.R. Tolkien"))
                    .andExpect(jsonPath("$.sortName").value("Tolkien, J.R.R."))
                    .andExpect(jsonPath("$.birthDate").value("1892-01-03"))
                    .andExpect(jsonPath("$.localizations[0].languageCode").value("pt-BR"));

            verify(authorService, times(1)).createAuthor(any(AuthorRequestDTO.class));
            verify(authorMapper, times(1)).toResponseDTO(any(Author.class));
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 400 Bad Request quando a lista de biografias estiver vazia (@NotEmpty)")
        void shouldReturn400WhenLocalizationsIsEmpty() throws Exception {

            AuthorRequestDTO invalidRequest = new AuthorRequestDTO(
                    "J.R.R. Tolkien",
                    "Tolkien, J.R.R.",
                    null,
                    "https://www.tolkienestate.com",
                    List.of()
            );

            mockMvc.perform(post("/api/v1/authors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.localizations").exists());

            verify(authorService, never()).createAuthor(any());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 400 Bad Request quando o displayName for vazio (@NotBlank)")
        void shouldReturn400WhenDisplayNameIsBlank() throws Exception {

            AuthorRequestDTO invalidRequest = new AuthorRequestDTO(
                    "",
                    "Tolkien, J.R.R.",
                    null,
                    "https://www.tolkienestate.com",
                    List.of(new AuthorLocalizationDTO("pt-BR", "Biografia aqui"))
            );

            mockMvc.perform(post("/api/v1/authors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors.displayName").exists());

            verify(authorService, never()).createAuthor(any());
        }
    }
}