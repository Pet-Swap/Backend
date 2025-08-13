// ProfileRepository.java
package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    Optional<Profile> findByUsername(String username);
    Boolean existsByUsername(String username);

}
