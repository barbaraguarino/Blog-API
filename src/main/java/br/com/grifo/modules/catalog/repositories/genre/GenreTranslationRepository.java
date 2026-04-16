package br.com.grifo.modules.catalog.repositories.genre;

import br.com.grifo.modules.catalog.domain.genre.GenreTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface GenreTranslationRepository extends JpaRepository<GenreTranslation, UUID> {
    boolean existsByLanguageCodeAndNameIgnoreCase(String languageCode, String name);
    List<GenreTranslation> findByLanguageCodeInAndNameInIgnoreCase(Collection<String> languageCodes, Collection<String> names);
}