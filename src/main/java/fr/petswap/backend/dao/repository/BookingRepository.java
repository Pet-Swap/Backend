package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    Optional<Booking> findByMatch(Match match);

    @Query("SELECT b FROM Booking b WHERE b.match.id = :matchId")
    Optional<Booking> findByMatchId(@Param("matchId") Integer matchId);

    List<Booking> findByStatus(Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.match.petSitter.id = :userId OR b.match.listing.owner.id = :userId")
    List<Booking> findBookingsForUser(@Param("userId") UUID userId);

    @Query("SELECT b FROM Booking b WHERE b.match.petSitter.id = :petSitterId")
    List<Booking> findBookingsByPetSitter(@Param("petSitterId") UUID petSitterId);

    @Query("SELECT b FROM Booking b WHERE b.match.listing.owner.id = :ownerId")
    List<Booking> findBookingsByOwner(@Param("ownerId") UUID ownerId);

    @Query("SELECT b FROM Booking b WHERE (b.match.petSitter.id = :userId OR b.match.listing.owner.id = :userId) AND b.status = :status")
    List<Booking> findBookingsForUserByStatus(@Param("userId") UUID userId, @Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.match.id = :matchId AND b.status = 'COMPLETED'")
    List<Booking> findCompletedBookingsByMatchId(@Param("matchId") Integer matchId);

    @Query("SELECT b FROM Booking b WHERE b.match.id = :matchId AND b.status IN ('CONFIRMED', 'IN_PROGRESS') AND ((b.startDate <= :endDate AND b.startDate >= :startDate) OR (b.endDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findConflictingBookings(@Param("matchId") Integer matchId, @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.match.listing.id = :listingId AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsByListingId(@Param("listingId") Integer listingId);

    @Query("SELECT b FROM Booking b WHERE b.match.listing.id = :listingId AND b.status IN ('CONFIRMED', 'IN_PROGRESS') AND ((b.startDate <= :endDate AND b.startDate >= :startDate) OR (b.endDate <= :endDate AND b.endDate >= :startDate))")
    List<Booking> findConflictingConfirmedBookings(@Param("listingId") Integer listingId, @Param("startDate") java.time.LocalDate startDate, @Param("endDate") java.time.LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.match.id = :matchId AND b.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    Optional<Booking> findActiveBookingByMatch(@Param("matchId") Integer matchId);
}
