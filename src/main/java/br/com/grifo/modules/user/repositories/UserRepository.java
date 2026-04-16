package br.com.grifo.modules.user.repositories;

import br.com.grifo.modules.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    boolean existsByGoogleId(String googleId);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
}
