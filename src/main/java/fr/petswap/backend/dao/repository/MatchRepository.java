package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Match;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.jpa.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, Integer> {
    List<Match> findByPetSitter(Profile petSitter);
    List<Match> findByListing_Owner(Profile owner);
    Optional<Match> findByListingAndPetSitter(Listing listing, Profile petSitter);

    @Query("SELECT m FROM Match m WHERE m.listing.owner.id = :userId OR m.petSitter.id = :userId")
    List<Match> findMatchesForUser(@Param("userId") UUID userId);

    @Query("SELECT m FROM Match m WHERE (m.listing.owner.id = :userId OR m.petSitter.id = :userId) AND m.ownerLikedBack = true")
    List<Match> findConfirmedMatchesForUser(@Param("userId") UUID userId);
}
