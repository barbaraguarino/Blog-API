package br.com.grifo.modules.user.domain;

import br.com.grifo.modules.user.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

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
    private boolean enabled = true;
    @Column(name = "is_locked")
    private boolean locked = false;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public boolean isLinkedToGoogle() {
        return this.googleId != null;
    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.getRole()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    @NonNull
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
