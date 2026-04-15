package br.com.grifo.modules.catalog.controllers;

import br.com.grifo.modules.catalog.domain.Genre;
import br.com.grifo.modules.catalog.dtos.GenreRequestDTO;
import br.com.grifo.modules.catalog.dtos.GenreResponseDTO;
import br.com.grifo.modules.catalog.mappers.GenreMapper;
import br.com.grifo.modules.catalog.services.GenreService;
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
