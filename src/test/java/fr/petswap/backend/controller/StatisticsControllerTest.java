package fr.petswap.backend.controller;

import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.MatchRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dao.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private StatisticsController statisticsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();

        // Configuration par défaut des mocks
        when(profileRepository.count()).thenReturn(150L);
        when(matchRepository.count()).thenReturn(75L);
        when(bookingRepository.count()).thenReturn(25L);
        when(reviewRepository.getAverageRating()).thenReturn(4.3);
    }

    @Test
    void getStatistics_ShouldReturnStatistics_WhenDataExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalUsers").value(150))
                .andExpect(jsonPath("$.totalMatches").value(75))
                .andExpect(jsonPath("$.totalBookings").value(25))
                .andExpect(jsonPath("$.averageRating").value(4.3));
    }

    @Test
    void getStatistics_ShouldReturnDefaultRating_WhenNoReviews() throws Exception {
        // Given
        when(reviewRepository.getAverageRating()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void getStatistics_ShouldReturnZeroValues_WhenNoData() throws Exception {
        // Given
        when(profileRepository.count()).thenReturn(0L);
        when(matchRepository.count()).thenReturn(0L);
        when(bookingRepository.count()).thenReturn(0L);
        when(reviewRepository.getAverageRating()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalUsers").value(0))
                .andExpect(jsonPath("$.totalMatches").value(0))
                .andExpect(jsonPath("$.totalBookings").value(0))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    void getStatistics_ShouldRoundRatingProperly() throws Exception {
        // Given
        when(reviewRepository.getAverageRating()).thenReturn(4.27);

        // When & Then
        mockMvc.perform(get("/api/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.3));
    }
}
