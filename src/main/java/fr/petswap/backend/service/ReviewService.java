package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.jpa.Review;
import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dao.repository.ReviewRepository;
import fr.petswap.backend.dto.CreateReviewRequest;
import fr.petswap.backend.dto.ReviewDto;
import fr.petswap.backend.dto.ReviewStatusDto;
import fr.petswap.backend.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ProfileRepository profileRepository;
    private final ReviewMapper reviewMapper;

    /**
     * Créer un avis après une réservation
     */
    public ReviewDto createReview(UUID reviewerId, CreateReviewRequest request) {
        log.info("Création d'un avis par l'utilisateur {} pour la réservation {}", reviewerId, request.getBookingId());

        // Vérifier que la réservation existe
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier que l'utilisateur peut laisser un avis (doit être participant à la réservation)
        UUID petSitterId = booking.getMatch().getPetSitter().getId();
        UUID ownerId = booking.getMatch().getListing().getOwner().getId();

        boolean canReview = petSitterId.equals(reviewerId) || ownerId.equals(reviewerId);

        if (!canReview) {
            throw new RuntimeException("Vous n'êtes pas autorisé à laisser un avis pour cette réservation");
        }

        // Vérifier que la réservation est terminée
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Vous ne pouvez laisser un avis que pour une réservation terminée");
        }

        // Vérifier qu'un avis n'existe pas déjà de cet utilisateur pour ce booking
        if (reviewRepository.findByBookingIdAndReviewerId(request.getBookingId(), reviewerId).isPresent()) {
            throw new RuntimeException("Vous avez déjà laissé un avis pour cette réservation");
        }

        // Déterminer qui est évalué (l'autre participant)
        UUID reviewedUserId = petSitterId.equals(reviewerId) ? ownerId : petSitterId;

        Profile reviewer = profileRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Utilisateur reviewer non trouvé"));

        Profile reviewedUser = profileRepository.findById(reviewedUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur évalué non trouvé"));

        Review review = Review.builder()
                .booking(booking)
                .reviewer(reviewer)
                .reviewedUser(reviewedUser)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Avis {} créé avec succès", savedReview.getId());

        // Mettre à jour la note moyenne de l'utilisateur évalué
        updateUserRating(reviewedUserId);

        return reviewMapper.toDto(savedReview);
    }

    /**
     * Récupérer tous les avis d'une réservation
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByBooking(Integer bookingId) {
        List<Review> reviews = reviewRepository.findByBookingId(bookingId);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Récupérer tous les avis reçus par un utilisateur
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsForUser(UUID userId) {
        List<Review> reviews = reviewRepository.findReviewsForUser(userId);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Récupérer tous les avis donnés par un utilisateur
     */
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByReviewer(UUID reviewerId) {
        List<Review> reviews = reviewRepository.findByReviewerId(reviewerId);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    /**
     * Vérifier si un utilisateur peut encore laisser un avis pour un booking
     */
    @Transactional(readOnly = true)
    public boolean canUserReviewBooking(UUID userId, Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier que l'utilisateur est participant
        boolean isParticipant = booking.getMatch().getPetSitter().getId().equals(userId) ||
                               booking.getMatch().getListing().getOwner().getId().equals(userId);

        if (!isParticipant) {
            return false;
        }

        // Vérifier que la réservation est terminée
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            return false;
        }

        // Vérifier qu'il n'a pas déjà laissé d'avis
        return reviewRepository.findByBookingIdAndReviewerId(bookingId, userId).isEmpty();
    }

    /**
     * Obtenir les statuts des reviews pour un booking (qui a reviewé qui)
     */
    @Transactional(readOnly = true)
    public ReviewStatusDto getReviewStatus(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        UUID petSitterId = booking.getMatch().getPetSitter().getId();
        UUID ownerId = booking.getMatch().getListing().getOwner().getId();

        boolean ownerReviewedSitter = reviewRepository.findByBookingIdAndReviewerId(bookingId, ownerId).isPresent();
        boolean sitterReviewedOwner = reviewRepository.findByBookingIdAndReviewerId(bookingId, petSitterId).isPresent();

        return ReviewStatusDto.builder()
                .bookingId(bookingId)
                .ownerId(ownerId)
                .petSitterId(petSitterId)
                .ownerReviewedSitter(ownerReviewedSitter)
                .sitterReviewedOwner(sitterReviewedOwner)
                .build();
    }

    /**
     * Mettre à jour la note moyenne d'un utilisateur
     */
    private void updateUserRating(UUID userId) {
        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Calculer la nouvelle note moyenne
        Double averageRating = reviewRepository.getAverageRatingForUser(userId);

        user.setRating(averageRating != null ? averageRating.floatValue() : 0.0f);
        profileRepository.save(user);

        log.info("Note moyenne mise à jour pour l'utilisateur {} : {}", userId, averageRating);
    }
}
