package br.com.grifo.modules.catalog.domain.author;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_authors")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String displayName;
    private String sortName;
    private LocalDate birthDate;
    private String website;

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AuthorLocalization> localizations = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static Author create(String displayName, String sortName, LocalDate birthDate, String website) {
        return Author.builder()
                .displayName(displayName)
                .sortName(sortName)
                .birthDate(birthDate)
                .website(website)
                .build();
    }

    public void addLocalization(AuthorLocalization localization) {
        this.localizations.add(localization);
        localization.assignToAuthor(this);
    }
}
