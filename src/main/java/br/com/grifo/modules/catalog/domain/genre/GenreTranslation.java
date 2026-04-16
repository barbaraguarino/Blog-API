package br.com.grifo.modules.catalog.domain.genre;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_genre_translations")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GenreTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    private String languageCode;
    private String name;

    public static GenreTranslation create(String languageCode, String name) {
        return GenreTranslation.builder()
                .languageCode(languageCode)
                .name(name)
                .build();
    }

    void assignToGenre(Genre genre) {
        this.genre = genre;
    }

}
