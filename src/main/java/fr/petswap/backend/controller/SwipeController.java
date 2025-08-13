package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.ListingDto;
import fr.petswap.backend.dto.MatchDto;
import fr.petswap.backend.dto.SwipeDto;
import fr.petswap.backend.dto.SwipeRequest;
import fr.petswap.backend.service.SwipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<SwipeDto> swipeOnListing(@RequestBody SwipeRequest request, Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        SwipeDto swipe = swipeService.swipeOnListing(currentUser.getId(), request);
        return ResponseEntity.ok(swipe);
    }

    @GetMapping("/discover")
    public ResponseEntity<List<ListingDto>> getUnswipedListings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<ListingDto> listings = swipeService.getUnswipedListings(currentUser.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/history")
    public ResponseEntity<List<SwipeDto>> getSwipeHistory(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<SwipeDto> swipes = swipeService.getSwipeHistory(currentUser.getId());
        return ResponseEntity.ok(swipes);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<MatchDto>> getMatches(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<MatchDto> matches = swipeService.getMatchesForUser(currentUser.getId());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/confirmed")
    public ResponseEntity<List<MatchDto>> getConfirmedMatches(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<MatchDto> matches = swipeService.getConfirmedMatchesForUser(currentUser.getId());
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/matches/pending")
    public ResponseEntity<List<MatchDto>> getPendingMatches(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<MatchDto> matches = swipeService.getPendingMatchesForOwner(currentUser.getId());
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/matches/{matchId}/respond")
    public ResponseEntity<MatchDto> respondToMatch(
            @PathVariable Integer matchId,
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        boolean accept = request.get("accept");
        MatchDto match = swipeService.respondToMatch(currentUser.getId(), matchId, accept);

        if (match == null) {
            return ResponseEntity.ok().build(); // Match rejeté et supprimé
        }
        return ResponseEntity.ok(match);
    }
}
