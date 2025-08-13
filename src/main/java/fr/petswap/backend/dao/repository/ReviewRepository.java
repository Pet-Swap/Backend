package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Review;
import fr.petswap.backend.dao.jpa.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByBooking(Booking booking);

    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId")
    List<Review> findByBookingId(@Param("bookingId") Integer bookingId);

    @Query("SELECT r FROM Review r WHERE r.reviewedUser.id = :userId")
    List<Review> findReviewsForUser(@Param("userId") UUID userId);

    List<Review> findByReviewerId(UUID reviewerId);

    @Query("SELECT r FROM Review r WHERE r.booking.id = :bookingId AND r.reviewer.id = :reviewerId")
    Optional<Review> findByBookingIdAndReviewerId(@Param("bookingId") Integer bookingId, @Param("reviewerId") UUID reviewerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewedUser.id = :userId")
    Double getAverageRatingForUser(@Param("userId") UUID userId);

    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getAverageRating();
}
