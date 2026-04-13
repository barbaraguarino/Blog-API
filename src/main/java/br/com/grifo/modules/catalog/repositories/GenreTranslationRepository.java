package br.com.grifo.modules.catalog.repositories;

import br.com.grifo.modules.catalog.domain.GenreTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenreTranslationRepository extends JpaRepository<GenreTranslation, UUID> {
    boolean existsByLanguageCodeAndNameIgnoreCase(String languageCode, String name);
}