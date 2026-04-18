package br.com.grifo.modules.catalog.domain.author;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_author_localizations")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorLocalization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    private String languageCode;
    private String biography;

    public static AuthorLocalization create(String languageCode, String biography) {
        return AuthorLocalization.builder()
                .languageCode(languageCode)
                .biography(biography)
                .build();
    }

    void assignToAuthor(Author author) {
        this.author = author;
    }
}
