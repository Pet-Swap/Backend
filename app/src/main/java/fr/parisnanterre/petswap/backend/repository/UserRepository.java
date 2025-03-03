package fr.parisnanterre.petswap.backend.repository;

import fr.parisnanterre.petswap.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
