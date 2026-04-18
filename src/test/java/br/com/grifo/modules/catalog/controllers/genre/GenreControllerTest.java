package br.com.grifo.modules.catalog.controllers.genre;

import br.com.grifo.core.exceptions.GlobalExceptionHandler;
import br.com.grifo.core.security.CustomUserDetailsService;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.modules.catalog.domain.genre.Genre;
import br.com.grifo.modules.catalog.dtos.genre.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.genre.GenreResponseDTO;
import br.com.grifo.modules.catalog.dtos.genre.GenreTranslationDTO;
import br.com.grifo.modules.catalog.mappers.genre.GenreMapper;
import br.com.grifo.modules.catalog.services.genre.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GenreControllerTest.MethodSecurityConfig.class})
class GenreControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig { }

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private GenreMapper genreMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Nested
    @DisplayName("Criação de gêneros")
    class Create{

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 201 CREATED ao criar gênero com sucesso")
        void shouldReturnCreatedWhenAdminAndPayloadValid() throws Exception {
            GenreTranslationDTO translationDTO1 = new GenreTranslationDTO("en-US", "Fantasy");
            GenreTranslationDTO translationDTO2 = new GenreTranslationDTO("pt-BR", "Fantasia");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO1, translationDTO2));

            var mockedGenre = Genre.createGenre();

            UUID generatedId = UUID.randomUUID();

            ReflectionTestUtils.setField(mockedGenre, "id", generatedId);

            GenreResponseDTO responseDTO = new GenreResponseDTO(generatedId, null, List.of(translationDTO1, translationDTO2));

            when(genreService.createGenre(any(GenreRequestDTO.class))).thenReturn(mockedGenre);
            when(genreMapper.toResponseDTO(any(Genre.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/genres")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.translations[0].name").value("Fantasy"))
                    .andExpect(jsonPath("$.translations[1].name").value("Fantasia"));
        }

        @Test
        @WithMockUser(authorities = "ROLE_READER")
        @DisplayName("Deve retornar 403 FORBIDDEN ao tentar criar gênero sem privilégios")
        void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Romance");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

            mockMvc.perform(post("/api/v1/genres")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.path").value("/api/v1/genres"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 400 BAD REQUEST ao enviar payload com formato de idioma inválido")
        void shouldReturnBadRequestWhenInvalidPayload() throws Exception {
            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-br", "");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

            mockMvc.perform(post("/api/v1/genres")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.path").value("/api/v1/genres"))
                    .andExpect(jsonPath("$.validationErrors['translations[0].languageCode']").exists())
                    .andExpect(jsonPath("$.validationErrors['translations[0].name']").exists());
        }
    }

    @Nested
    @DisplayName("Criação de subgêneros")
    class CreateSubgenre {

        private UUID parentId;

        @BeforeEach
        void setUp() {
            parentId = UUID.randomUUID();
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 201 CREATED ao criar subgênero com sucesso")
        void shouldReturnCreatedWhenAdminAndPayloadValid() throws Exception {

            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Alta Fantasia");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

            var mockedGenre = Genre.createGenre();
            UUID generatedId = UUID.randomUUID();

            ReflectionTestUtils.setField(mockedGenre, "id", generatedId);

            GenreResponseDTO responseDTO = new GenreResponseDTO(generatedId, null, List.of(translationDTO));

            when(genreService.createSubgenre(org.mockito.ArgumentMatchers.eq(parentId), any(GenreRequestDTO.class))).thenReturn(mockedGenre);
            when(genreMapper.toResponseDTO(any(Genre.class))).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/genres/{parentId}/subgenres", parentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.translations[0].name").value("Alta Fantasia"));
        }

        @Test
        @WithMockUser(authorities = "ROLE_READER")
        @DisplayName("Deve retornar 403 FORBIDDEN ao tentar criar subgênero sem privilégios")
        void shouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {

            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Ficção Científica");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

            mockMvc.perform(post("/api/v1/genres/{parentId}/subgenres", parentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.error").value("Forbidden"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @WithMockUser(authorities = "ROLE_ADMIN")
        @DisplayName("Deve retornar 400 BAD REQUEST ao enviar payload com formato de idioma inválido")
        void shouldReturnBadRequestWhenInvalidPayload() throws Exception {

            GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-br", "");
            GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

            mockMvc.perform(post("/api/v1/genres/{parentId}/subgenres", parentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.validationErrors['translations[0].languageCode']").exists())
                    .andExpect(jsonPath("$.validationErrors['translations[0].name']").exists());
        }
    }

}