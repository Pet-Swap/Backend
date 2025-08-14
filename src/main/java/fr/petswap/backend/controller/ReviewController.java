package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.CreateReviewRequest;
import fr.petswap.backend.dto.ReviewDto;
import fr.petswap.backend.dto.ReviewStatusDto;
import fr.petswap.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        ReviewDto review = reviewService.createReview(currentUser.getId(), request);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByBooking(@PathVariable Integer bookingId) {
        List<ReviewDto> reviews = reviewService.getReviewsByBooking(bookingId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/booking/{bookingId}/status")
    public ResponseEntity<ReviewStatusDto> getReviewStatus(@PathVariable Integer bookingId) {
        ReviewStatusDto status = reviewService.getReviewStatus(bookingId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/booking/{bookingId}/can-review")
    public ResponseEntity<Boolean> canReviewBooking(
            @PathVariable Integer bookingId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        boolean canReview = reviewService.canUserReviewBooking(currentUser.getId(), bookingId);
        return ResponseEntity.ok(canReview);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForUser(@PathVariable UUID userId) {
        List<ReviewDto> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDto>> getMyReviews(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<ReviewDto> reviews = reviewService.getReviewsByReviewer(currentUser.getId());
        return ResponseEntity.ok(reviews);
    }
}
