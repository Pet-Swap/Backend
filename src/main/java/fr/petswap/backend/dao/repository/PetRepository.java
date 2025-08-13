package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Pet;
import fr.petswap.backend.dao.jpa.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Integer> {
    List<Pet> findByOwner(Profile owner);
    List<Pet> findByOwnerId(java.util.UUID ownerId);
}
