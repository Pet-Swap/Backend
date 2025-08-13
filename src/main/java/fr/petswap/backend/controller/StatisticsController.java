package fr.petswap.backend.controller;

import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dao.repository.MatchRepository;
import fr.petswap.backend.dao.repository.BookingRepository;
import fr.petswap.backend.dao.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = profileRepository.count();
        long totalMatches = matchRepository.count();
        long totalBookings = bookingRepository.count();
        // Note moyenne
        Double averageRating = reviewRepository.getAverageRating();
        if (averageRating == null) averageRating = 4.5;
        stats.put("totalUsers", totalUsers);
        stats.put("totalMatches", totalMatches);
        stats.put("totalBookings", totalBookings);
        stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
        return stats;
    }
}

