package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dao.jpa.Match;
import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.MatchRepository;
import fr.petswap.backend.dto.BookingDto;
import fr.petswap.backend.dto.CreateBookingRequest;
import fr.petswap.backend.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final MatchRepository matchRepository;
    private final BookingMapper bookingMapper;
    private final ListingStatusService listingStatusService;

    /**
     * Créer une demande de garde à partir d'un match confirmé (par le propriétaire)
     */
    public BookingDto createBooking(UUID userId, CreateBookingRequest request) {
        log.info("Création d'une demande de garde par le propriétaire {} pour le match {}", userId, request.getMatchId());

        // Vérifier que le match existe et est confirmé
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        if (!match.isOwnerLikedBack()) {
            throw new RuntimeException("Le match doit être confirmé pour créer une demande de garde");
        }

        // Vérifier que l'utilisateur est le propriétaire de l'annonce
        if (!match.getListing().getOwner().getId().equals(userId)) {
            throw new RuntimeException("Seul le propriétaire peut faire une demande de garde");
        }

        // Vérifier que l'annonce est encore active ou déjà réservée (multiples réservations possibles)
        if (match.getListing().getStatus() == Listing.ListingStatus.COMPLETED ||
            match.getListing().getStatus() == Listing.ListingStatus.INACTIVE) {
            throw new RuntimeException("Cette annonce n'est plus disponible");
        }

        // Vérifier qu'il n'y a pas déjà une réservation active pour ce match
        Optional<Booking> existingActiveBooking = bookingRepository.findActiveBookingByMatch(request.getMatchId());
        if (existingActiveBooking.isPresent()) {
            throw new RuntimeException("Une demande de garde active existe déjà pour ce match");
        }

        // Vérifier que les dates ne sont pas dans le passé
        if (request.getStartDate().isBefore(java.time.LocalDate.now()) ||
            request.getEndDate().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("Les dates de garde ne peuvent pas être dans le passé");
        }

        // Vérifier les dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("La date de début doit être antérieure à la date de fin");
        }

        if (request.getStartDate().isBefore(match.getListing().getStartDate()) ||
            request.getEndDate().isAfter(match.getListing().getEndDate())) {
            throw new RuntimeException("Les dates de garde doivent être dans la période de l'annonce");
        }

        // Vérifier les conflits avec d'autres réservations confirmées
        List<Booking> conflictingBookings = bookingRepository.findConflictingConfirmedBookings(
            match.getListing().getId(), request.getStartDate(), request.getEndDate());
        if (!conflictingBookings.isEmpty()) {
            throw new RuntimeException("Ces dates sont déjà réservées");
        }

        // Calculer le prix total
        long numberOfDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        double totalPrice = numberOfDays * match.getListing().getPricePerDay();

        Booking booking = Booking.builder()
                .match(match)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Demande de garde {} créée avec succès pour {} jours au prix de {}€ - En attente de confirmation du pet-sitter",
                savedBooking.getId(), numberOfDays, totalPrice);

        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Confirmer une demande de garde (par le pet-sitter)
     */
    public BookingDto confirmBooking(UUID userId, Integer bookingId) {
        log.info("Confirmation de la demande de garde {} par le pet-sitter {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Demande de garde non trouvée"));

        // Vérifier que l'utilisateur est le pet-sitter du match
        if (!booking.getMatch().getPetSitter().getId().equals(userId)) {
            throw new RuntimeException("Seul le pet-sitter peut accepter cette demande de garde");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Seule une demande en attente peut être confirmée");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        // Mettre à jour le statut de l'annonce (ACTIVE → RESERVED)
        listingStatusService.updateListingStatus(booking.getMatch().getListing().getId());

        log.info("Demande de garde {} acceptée par le pet-sitter", bookingId);

        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Marquer une réservation comme terminée
     */
    public BookingDto completeBooking(UUID userId, Integer bookingId) {
        log.info("Finalisation de la réservation {} par l'utilisateur {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier que l'utilisateur fait partie de la réservation
        boolean isParticipant = booking.getMatch().getPetSitter().getId().equals(userId) ||
                               booking.getMatch().getListing().getOwner().getId().equals(userId);
        if (!isParticipant) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette réservation");
        }

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED &&
            booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            throw new RuntimeException("Seule une réservation confirmée ou en cours peut être terminée");
        }

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        Booking savedBooking = bookingRepository.save(booking);

        // Mettre à jour le statut de l'annonce (potentiellement RESERVED → COMPLETED)
        listingStatusService.updateListingStatus(booking.getMatch().getListing().getId());

        log.info("Réservation {} terminée", bookingId);

        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Refuser une demande de garde (par le pet-sitter)
     */
    public BookingDto rejectBooking(UUID userId, Integer bookingId) {
        log.info("Refus de la demande de garde {} par le pet-sitter {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Demande de garde non trouvée"));

        // Vérifier que l'utilisateur est le pet-sitter du match
        if (!booking.getMatch().getPetSitter().getId().equals(userId)) {
            throw new RuntimeException("Seul le pet-sitter peut refuser cette demande de garde");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Seule une demande en attente peut être refusée");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Demande de garde {} refusée par le pet-sitter", bookingId);

        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Annuler une réservation avec permissions adaptées au nouveau flux
     */
    public BookingDto cancelBooking(UUID userId, Integer bookingId) {
        log.info("Annulation de la réservation {} par l'utilisateur {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        boolean isOwner = booking.getMatch().getListing().getOwner().getId().equals(userId);
        boolean isPetSitter = booking.getMatch().getPetSitter().getId().equals(userId);

        if (!isOwner && !isPetSitter) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette réservation");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED ||
            booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Cette réservation ne peut plus être annulée");
        }

        // Permissions spécifiques selon le statut
        if (booking.getStatus() == Booking.BookingStatus.PENDING) {
            if (!isOwner) {
                throw new RuntimeException("Seul le propriétaire peut annuler sa demande en attente");
            }
        }
        // Pour les réservations confirmées, les deux peuvent annuler

        Booking.BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        // Si on annule une réservation confirmée, recalculer le statut de l'annonce
        if (previousStatus == Booking.BookingStatus.CONFIRMED) {
            listingStatusService.updateListingStatus(booking.getMatch().getListing().getId());
        }

        log.info("Réservation {} annulée", bookingId);

        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Récupérer une réservation par ID
     */
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        return bookingMapper.toDto(booking);
    }

    /**
     * Récupérer toutes les réservations d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForUser(UUID userId) {
        List<Booking> bookings = bookingRepository.findBookingsForUser(userId);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les réservations d'un utilisateur par statut
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForUserByStatus(UUID userId, Booking.BookingStatus status) {
        List<Booking> bookings = bookingRepository.findBookingsForUserByStatus(userId, status);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les réservations en tant que pet-sitter
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsAsPetSitter(UUID petSitterId) {
        List<Booking> bookings = bookingRepository.findBookingsByPetSitter(petSitterId);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les réservations en tant que propriétaire
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsAsOwner(UUID ownerId) {
        List<Booking> bookings = bookingRepository.findBookingsByOwner(ownerId);
        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    /**
     * Re-booking d'une réservation existante
     */
    public BookingDto createRebooking(UUID userId, fr.petswap.backend.dto.RebookingRequest request) {
        log.info("Tentative de rebooking pour le match {} par {}", request.getMatchId(), userId);
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));
        // Vérifier que l'utilisateur fait partie du match
        boolean isParticipant = match.getPetSitter().getId().equals(userId) ||
                match.getListing().getOwner().getId().equals(userId);
        if (!isParticipant) {
            throw new RuntimeException("Vous n'êtes pas autorisé à rebook ce match");
        }
        // Vérifier qu'il existe au moins une réservation complétée
        var completedBookings = bookingRepository.findCompletedBookingsByMatchId(request.getMatchId());
        if (completedBookings.isEmpty()) {
            throw new RuntimeException("Aucune réservation complétée trouvée pour ce match");
        }
        // Vérifier les dates
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new RuntimeException("La date de début doit être antérieure à la date de fin");
        }
        // Vérifier les conflits de dates
        var conflicts = bookingRepository.findConflictingBookings(request.getMatchId(), request.getStartDate(), request.getEndDate());
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Conflit de dates avec une réservation existante");
        }
        // Calculer le prix total
        long numberOfDays = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        double totalPrice = numberOfDays * match.getListing().getPricePerDay();
        Booking booking = Booking.builder()
                .match(match)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalPrice(totalPrice)
                .status(Booking.BookingStatus.CONFIRMED)
                .isRebooking(true)
                .originalBooking(completedBookings.get(0))
                .specialRequests(request.getSpecialRequests())
                .build();
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Rebooking {} créé et confirmé automatiquement", savedBooking.getId());
        return bookingMapper.toDto(savedBooking);
    }
}
