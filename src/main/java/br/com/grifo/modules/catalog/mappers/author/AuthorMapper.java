package br.com.grifo.modules.catalog.mappers.author;

import br.com.grifo.modules.catalog.domain.author.Author;
import br.com.grifo.modules.catalog.dtos.author.AuthorLocalizationDTO;
import br.com.grifo.modules.catalog.dtos.author.AuthorResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorMapper {

    public AuthorResponseDTO toResponseDTO(Author author) {
        List<AuthorLocalizationDTO> localizationDTOs = author.getLocalizations().stream()
                .map(loc -> new AuthorLocalizationDTO(loc.getLanguageCode(), loc.getBiography()))
                .toList();

        return new AuthorResponseDTO(
                author.getId(),
                author.getDisplayName(),
                author.getSortName(),
                author.getBirthDate(),
                author.getWebsite(),
                localizationDTOs
        );
    }
}
