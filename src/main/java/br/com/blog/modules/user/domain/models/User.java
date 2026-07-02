package br.com.blog.modules.user.domain.models;

import br.com.blog.modules.user.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String email;
    private String nickname;
    private String password;
    private String googleId;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_enabled")
    private boolean enabled;

    @Column(name = "is_locked")
    private boolean locked;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static User createLocalUser(
            String name,
            String email,
            String encodedPassword
    ) {
        return User.builder()
                .name(name)
                .email(email)
                .password(encodedPassword)
                .nickname(generateRandomNickname(name))
                .enabled(true)
                .locked(false)
                .role(UserRole.READER)
                .build();
    }

    public static User createGoogleUser(
            String name,
            String email,
            String googleId
    ) {
        return User.builder()
                .name(name)
                .email(email)
                .googleId(googleId)
                .enabled(true)
                .locked(false)
                .nickname(generateRandomNickname(name))
                .role(UserRole.READER)
                .build();
    }

    public boolean isLinkedToGoogle() {
        return this.googleId != null;
    }

    private static String generateRandomNickname(String name) {
        String cleanName = name.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");

        if (cleanName.length() > 40) {
            cleanName = cleanName.substring(0, 40);
        }

        SecureRandom random = new SecureRandom();
        int suffix = 1000 + random.nextInt(9000);

        return cleanName + "_" + suffix;
    }
}
