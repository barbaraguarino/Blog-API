package br.com.grifo.modules.catalog.services.author;

import br.com.grifo.modules.catalog.domain.author.Author;
import br.com.grifo.modules.catalog.domain.author.AuthorLocalization;
import br.com.grifo.modules.catalog.dtos.author.AuthorRequestDTO;
import br.com.grifo.modules.catalog.repositories.author.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    public Author createAuthor(AuthorRequestDTO dto) {

        Author author = Author.create(
                dto.displayName(),
                dto.sortName(),
                dto.birthDate(),
                dto.website()
        );

        dto.localizations().forEach(locDto -> {
            AuthorLocalization localization = AuthorLocalization.create(
                    locDto.languageCode(),
                    locDto.biography()
            );
            author.addLocalization(localization);
        });

        return authorRepository.save(author);
    }
}
