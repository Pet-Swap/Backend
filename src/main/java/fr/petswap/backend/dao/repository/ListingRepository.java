package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dao.jpa.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, Integer> {
    List<Listing> findByOwner(Profile owner);
    List<Listing> findByOwnerId(UUID ownerId);
    List<Listing> findByStatus(Listing.ListingStatus status);

    @Query("SELECT l FROM Listing l WHERE l.status = 'ACTIVE' AND l.owner.id != :userId")
    List<Listing> findActiveListingsExcludingUser(@Param("userId") UUID userId);
}
