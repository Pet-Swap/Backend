package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dao.jpa.Match;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.jpa.Review;
import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dao.repository.ReviewRepository;
import fr.petswap.backend.dto.CreateReviewRequest;
import fr.petswap.backend.dto.ReviewDto;
import fr.petswap.backend.dto.ReviewStatusDto;
import fr.petswap.backend.mapper.ReviewMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewerId;
    private UUID reviewedUserId;
    private UUID ownerId;
    private UUID petSitterId;
    private CreateReviewRequest request;
    private Booking booking;
    private Profile reviewer;
    private Profile reviewedUser;
    private Review review;
    private ReviewDto reviewDto;

    @BeforeEach
    void setUp() {
        reviewerId = UUID.randomUUID();
        reviewedUserId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        petSitterId = UUID.randomUUID();

        request = new CreateReviewRequest();
        request.setBookingId(1);
        request.setRating(5);
        request.setComment("Excellent service !");

        // Setup booking with match
        Profile owner = new Profile();
        owner.setId(ownerId);

        Profile petSitter = new Profile();
        petSitter.setId(petSitterId);

        Listing listing = new Listing();
        listing.setOwner(owner);

        Match match = new Match();
        match.setListing(listing);
        match.setPetSitter(petSitter);

        booking = new Booking();
        booking.setId(1);
        booking.setMatch(match);
        booking.setStatus(Booking.BookingStatus.COMPLETED);

        reviewer = new Profile();
        reviewer.setId(reviewerId);
        reviewer.setUsername("reviewer");

        reviewedUser = new Profile();
        reviewedUser.setId(reviewedUserId);
        reviewedUser.setUsername("reviewed");

        review = new Review();
        review.setId(1);
        review.setBooking(booking);
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewedUser);
        review.setRating(5);
        review.setComment("Excellent service !");

        reviewDto = new ReviewDto();
        reviewDto.setId(1);
        reviewDto.setBookingId(1);
        reviewDto.setReviewerId(reviewerId);
        reviewDto.setRating(5);
        reviewDto.setComment("Excellent service !");
    }

    @Test
    void createReview_ShouldCreateReview_WhenValidRequest() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingIdAndReviewerId(1, petSitterId)).thenReturn(Optional.empty());
        when(profileRepository.findById(petSitterId)).thenReturn(Optional.of(reviewer));
        when(profileRepository.findById(ownerId)).thenReturn(Optional.of(reviewedUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        // When
        ReviewDto result = reviewService.createReview(petSitterId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void createReview_ShouldThrowException_WhenBookingNotFound() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(reviewerId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Réservation non trouvée");
    }

    @Test
    void createReview_ShouldThrowException_WhenUserNotParticipant() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(unauthorizedUserId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vous n'êtes pas autorisé à laisser un avis pour cette réservation");
    }

    @Test
    void createReview_ShouldThrowException_WhenBookingNotCompleted() {
        // Given
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(petSitterId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vous ne pouvez laisser un avis que pour une réservation terminée");
    }

    @Test
    void createReview_ShouldThrowException_WhenReviewAlreadyExists() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingIdAndReviewerId(1, petSitterId))
                .thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(petSitterId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Vous avez déjà laissé un avis pour cette réservation");
    }

    @Test
    void getReviewsByBooking_ShouldReturnReviews_WhenBookingHasReviews() {
        // Given
        List<Review> reviews = Arrays.asList(review);
        when(reviewRepository.findByBookingId(1)).thenReturn(reviews);
        when(reviewMapper.toDto(review)).thenReturn(reviewDto);

        // When
        List<ReviewDto> result = reviewService.getReviewsByBooking(1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isEqualTo(5);
    }

    @Test
    void canUserReviewBooking_ShouldReturnTrue_WhenUserCanReview() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingIdAndReviewerId(1, petSitterId))
                .thenReturn(Optional.empty());

        // When
        boolean result = reviewService.canUserReviewBooking(petSitterId, 1);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void canUserReviewBooking_ShouldReturnFalse_WhenUserNotParticipant() {
        // Given
        UUID unauthorizedUserId = UUID.randomUUID();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When
        boolean result = reviewService.canUserReviewBooking(unauthorizedUserId, 1);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canUserReviewBooking_ShouldReturnFalse_WhenBookingNotCompleted() {
        // Given
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));

        // When
        boolean result = reviewService.canUserReviewBooking(petSitterId, 1);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void canUserReviewBooking_ShouldReturnFalse_WhenReviewAlreadyExists() {
        // Given
        when(bookingRepository.findById(1)).thenReturn(Optional.of(booking));
        when(reviewRepository.findByBookingIdAndReviewerId(1, petSitterId))
                .thenReturn(Optional.of(review));

        // When
        boolean result = reviewService.canUserReviewBooking(petSitterId, 1);

        // Then
        assertThat(result).isFalse();
    }
}