package fr.petswap.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.petswap.backend.config.CustomExceptionHandler;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.CreateReviewRequest;
import fr.petswap.backend.dto.ReviewDto;
import fr.petswap.backend.dto.ReviewStatusDto;
import fr.petswap.backend.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReviewController reviewController;

    private ObjectMapper objectMapper;
    private CreateReviewRequest createReviewRequest;
    private ReviewDto reviewDto;
    private Profile currentUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Configuration de MockMvc avec validation
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setControllerAdvice(new CustomExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        userId = UUID.randomUUID();
        currentUser = new Profile();
        currentUser.setId(userId);
        currentUser.setUsername("testuser");

        createReviewRequest = new CreateReviewRequest();
        createReviewRequest.setBookingId(1);
        createReviewRequest.setRating(5);
        createReviewRequest.setComment("Excellent service !");

        reviewDto = new ReviewDto();
        reviewDto.setId(1);
        reviewDto.setBookingId(1);
        reviewDto.setReviewerId(userId);
        reviewDto.setReviewerUsername("testuser");
        reviewDto.setRating(5);
        reviewDto.setComment("Excellent service !");
        reviewDto.setCreatedAt(Instant.now());
    }

    @Test
    void createReview_ShouldReturnCreatedReview_WhenValidRequest() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(reviewService.createReview(eq(userId), any(CreateReviewRequest.class)))
                .thenReturn(reviewDto);

        // When & Then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingId").value(1))
                .andExpect(jsonPath("$.reviewerId").value(userId.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent service !"));
    }

    @Test
    void createReview_ShouldReturnBadRequest_WhenInvalidRating() throws Exception {
        // Given
        createReviewRequest.setRating(6); // Invalid rating

        // When & Then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReviewRequest))
                        .principal(authentication))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getReviewsByBooking_ShouldReturnReviewsList_WhenBookingExists() throws Exception {
        // Given
        ReviewDto review2 = new ReviewDto();
        review2.setId(2);
        review2.setBookingId(1);
        review2.setRating(4);

        List<ReviewDto> reviews = Arrays.asList(reviewDto, review2);
        when(reviewService.getReviewsByBooking(1)).thenReturn(reviews);

        // When & Then
        mockMvc.perform(get("/api/reviews/booking/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getReviewStatus_ShouldReturnStatus_WhenBookingExists() throws Exception {
        // Given
        ReviewStatusDto statusDto = ReviewStatusDto.builder()
                .bookingId(1)
                .ownerId(UUID.randomUUID())
                .petSitterId(UUID.randomUUID())
                .ownerReviewedSitter(true)
                .sitterReviewedOwner(false)
                .build();

        when(reviewService.getReviewStatus(1)).thenReturn(statusDto);

        // When & Then
        mockMvc.perform(get("/api/reviews/booking/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookingId").value(1))
                .andExpect(jsonPath("$.ownerReviewedSitter").value(true))
                .andExpect(jsonPath("$.sitterReviewedOwner").value(false));
    }

    @Test
    void canReviewBooking_ShouldReturnTrue_WhenUserCanReview() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(reviewService.canUserReviewBooking(userId, 1)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/reviews/booking/1/can-review")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void canReviewBooking_ShouldReturnFalse_WhenUserCannotReview() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(reviewService.canUserReviewBooking(userId, 1)).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/reviews/booking/1/can-review")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(false));
    }
}
