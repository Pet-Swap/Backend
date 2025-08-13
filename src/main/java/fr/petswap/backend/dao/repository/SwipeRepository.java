package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Swipe;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.jpa.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SwipeRepository extends JpaRepository<Swipe, Integer> {
    List<Swipe> findBySwiper(Profile swiper);
    List<Swipe> findBySwiperAndDirection(Profile swiper, Swipe.SwipeDirection direction);
    Optional<Swipe> findBySwiperAndListing(Profile swiper, Listing listing);

    @Query("SELECT s FROM Swipe s WHERE s.listing.id = :listingId AND s.direction = 'LIKE'")
    List<Swipe> findLikesForListing(@Param("listingId") Integer listingId);

    @Query(value = "SELECT * FROM listings l WHERE l.status = 'ACTIVE' AND l.owner_id != :userId " +
           "AND l.listing_id NOT IN (SELECT s.listing_id FROM swipes s WHERE s.swiper_id = :userId)",
           nativeQuery = true)
    List<Listing> findUnswipedListingsForUser(@Param("userId") UUID userId);
}
