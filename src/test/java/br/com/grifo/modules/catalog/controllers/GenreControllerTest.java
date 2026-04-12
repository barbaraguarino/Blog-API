package br.com.grifo.modules.catalog.controllers;

import br.com.grifo.core.exceptions.GlobalExceptionHandler;
import br.com.grifo.core.security.CustomUserDetailsService;
import br.com.grifo.core.security.JwtAuthenticationFilter;
import br.com.grifo.core.security.JwtTokenProvider;
import br.com.grifo.core.security.SecurityConfig;
import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.dtos.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.GenreResponseDTO;
import br.com.grifo.modules.catalog.dtos.GenreTranslationDTO;
import br.com.grifo.modules.catalog.mappers.GenreMapper;
import br.com.grifo.modules.catalog.services.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private GenreMapper genreMapper;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Deve retornar 201 CREATED ao criar gênero com sucesso")
    void ShouldReturnCreated_WhenAdminAndPayloadValid() throws Exception {
        GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Ficção");
        GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

        Genre mockedGenre = new Genre();
        GenreResponseDTO responseDTO = new GenreResponseDTO(null, List.of(translationDTO));

        when(genreService.createGenre(any(GenreRequestDTO.class))).thenReturn(mockedGenre);
        when(genreMapper.toResponseDTO(any(Genre.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/genres").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.translations[0].name").value("Ficção"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_READER")
    @DisplayName("Deve retornar 403 FORBIDDEN ao tentar criar gênero sem privilégios")
    void ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-BR", "Romance");
        GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

        mockMvc.perform(post("/api/v1/genres").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Deve retornar 400 BAD REQUEST ao enviar payload com formato de idioma inválido")
    void ShouldReturnBadRequest_WhenInvalidPayload() throws Exception {
        GenreTranslationDTO translationDTO = new GenreTranslationDTO("pt-br", "Terror");
        GenreRequestDTO requestDTO = new GenreRequestDTO(List.of(translationDTO));

        mockMvc.perform(post("/api/v1/genres").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors['translations[0].languageCode']").exists());
    }
}