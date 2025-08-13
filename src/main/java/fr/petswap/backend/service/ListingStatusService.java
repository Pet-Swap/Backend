package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service pour gérer la logique des statuts d'annonce de manière cohérente
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ListingStatusService {

    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;

    /**
     * Met à jour le statut d'une annonce en fonction de ses réservations
     */
    public void updateListingStatus(Integer listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        // Récupérer toutes les réservations confirmées pour cette annonce
        List<Booking> confirmedBookings = bookingRepository.findConfirmedBookingsByListingId(listingId);

        if (confirmedBookings.isEmpty()) {
            // Aucune réservation confirmée → ACTIVE
            if (listing.getStatus() != Listing.ListingStatus.INACTIVE) {
                listing.setStatus(Listing.ListingStatus.ACTIVE);
                log.info("Annonce {} mise à jour vers ACTIVE", listingId);
            }
        } else {
            // Il y a des réservations confirmées → RESERVED
            listing.setStatus(Listing.ListingStatus.RESERVED);
            log.info("Annonce {} mise à jour vers RESERVED", listingId);
        }

        // Vérifier si toutes les réservations sont terminées et dans le passé
        boolean allBookingsCompleted = confirmedBookings.stream()
                .allMatch(booking ->
                    booking.getStatus() == Booking.BookingStatus.COMPLETED &&
                    booking.getEndDate().isBefore(LocalDate.now())
                );

        if (allBookingsCompleted && !confirmedBookings.isEmpty() &&
            listing.getEndDate().isBefore(LocalDate.now())) {
            listing.setStatus(Listing.ListingStatus.COMPLETED);
            log.info("Annonce {} mise à jour vers COMPLETED", listingId);
        }

        listingRepository.save(listing);
    }

    /**
     * Vérifie si une annonce peut accepter de nouvelles réservations
     */
    public boolean canAcceptNewBookings(Integer listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        return listing.getStatus() == Listing.ListingStatus.ACTIVE ||
               listing.getStatus() == Listing.ListingStatus.RESERVED;
    }

    /**
     * Vérifie si des dates spécifiques sont disponibles pour une annonce
     */
    public boolean areDatesAvailable(Integer listingId, LocalDate startDate, LocalDate endDate) {
        List<Booking> conflictingBookings = bookingRepository.findConflictingConfirmedBookings(
            listingId, startDate, endDate);
        return conflictingBookings.isEmpty();
    }

    /**
     * Désactive une annonce manuellement
     */
    public void deactivateListing(Integer listingId, String reason) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        listing.setStatus(Listing.ListingStatus.INACTIVE);
        listingRepository.save(listing);
        log.info("Annonce {} désactivée : {}", listingId, reason);
    }

    /**
     * Réactive une annonce
     */
    public void reactivateListing(Integer listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        if (listing.getStatus() == Listing.ListingStatus.INACTIVE) {
            // Recalculer le statut approprié
            updateListingStatus(listingId);
            log.info("Annonce {} réactivée", listingId);
        }
    }
}
