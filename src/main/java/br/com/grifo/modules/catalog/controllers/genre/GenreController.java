package br.com.grifo.modules.catalog.controllers.genre;

import br.com.grifo.modules.catalog.dtos.genre.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.genre.GenreResponseDTO;
import br.com.grifo.modules.catalog.mappers.genre.GenreMapper;
import br.com.grifo.modules.catalog.services.genre.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;
    private final GenreMapper genreMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponseDTO> create(@RequestBody @Valid GenreRequestDTO dto) {

        var response = genreMapper.toResponseDTO(genreService.createGenre(dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{parentId}/subgenres")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponseDTO> createSubgenre(
            @PathVariable UUID parentId,
            @Valid @RequestBody GenreRequestDTO requestDTO) {

        var responseDTO = genreMapper.toResponseDTO(genreService.createSubgenre(parentId, requestDTO));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
