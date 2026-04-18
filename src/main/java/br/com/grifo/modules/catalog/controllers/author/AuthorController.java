package br.com.grifo.modules.catalog.controllers.author;

import br.com.grifo.modules.catalog.dtos.author.AuthorRequestDTO;
import br.com.grifo.modules.catalog.dtos.author.AuthorResponseDTO;
import br.com.grifo.modules.catalog.mappers.author.AuthorMapper;
import br.com.grifo.modules.catalog.services.author.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponseDTO> create(@RequestBody @Valid AuthorRequestDTO dto) {

        var response = authorMapper.toResponseDTO(authorService.createAuthor(dto));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
