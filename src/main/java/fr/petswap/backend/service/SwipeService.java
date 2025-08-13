package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.*;
import fr.petswap.backend.dao.repository.*;
import fr.petswap.backend.dto.ListingDto;
import fr.petswap.backend.dto.MatchDto;
import fr.petswap.backend.dto.SwipeDto;
import fr.petswap.backend.dto.SwipeRequest;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.mapper.ListingMapper;
import fr.petswap.backend.mapper.MatchMapper;
import fr.petswap.backend.mapper.SwipeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final ListingRepository listingRepository;
    private final ProfileRepository profileRepository;
    private final SwipeMapper swipeMapper;
    private final MatchMapper matchMapper;
    private final ListingMapper listingMapper;

    /**
     * Un pet sitter swipe sur une annonce (listing)
     */
    @Transactional
    public SwipeDto swipeOnListing(UUID swiperId, SwipeRequest request) {
        log.info("Swipe de l'utilisateur {} sur l'annonce {}", swiperId, request.getListingId());

        Profile swiper = profileRepository.findById(swiperId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        // Vérifier qu'on ne peut pas swiper sur sa propre annonce
        if (listing.getOwner().getId().equals(swiperId)) {
            throw new RuntimeException("Vous ne pouvez pas swiper sur votre propre annonce");
        }

        // Vérifier qu'on n'a pas déjà swipé sur cette annonce
        Optional<Swipe> existingSwipe = swipeRepository.findBySwiperAndListing(swiper, listing);
        if (existingSwipe.isPresent()) {
            throw new RuntimeException("Vous avez déjà swipé sur cette annonce");
        }

        Swipe swipe = Swipe.builder()
                .swiper(swiper)
                .listing(listing)
                .direction(Swipe.SwipeDirection.valueOf(request.getDirection()))
                .build();

        Swipe savedSwipe = swipeRepository.save(swipe);

        // Si c'est un LIKE, créer un match (en attente de confirmation du propriétaire)
        if (Swipe.SwipeDirection.LIKE.equals(savedSwipe.getDirection())) {
            createPendingMatch(listing, swiper);
        }

        log.info("Swipe {} enregistré", savedSwipe.getId());
        return swipeMapper.toDto(savedSwipe);
    }

    /**
     * Créer un match en attente (pet sitter a liké, propriétaire doit confirmer)
     */
    private void createPendingMatch(Listing listing, Profile petSitter) {
        // Vérifier qu'il n'y a pas déjà un match
        Optional<Match> existingMatch = matchRepository.findByListingAndPetSitter(listing, petSitter);
        if (existingMatch.isEmpty()) {
            Match match = Match.builder()
                    .listing(listing)
                    .petSitter(petSitter)
                    .ownerLikedBack(false)
                    .build();

            matchRepository.save(match);
            log.info("Match en attente créé entre {} et l'annonce {}", petSitter.getUsername(), listing.getId());
        }
    }

    /**
     * Le propriétaire confirme ou rejette un match
     */
    @Transactional
    public MatchDto respondToMatch(UUID ownerId, Integer matchId, boolean accept) {
        log.info("Réponse du propriétaire {} au match {} : {}", ownerId, matchId, accept ? "accepté" : "rejeté");

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        // Vérifier que l'utilisateur est bien le propriétaire de l'annonce
        if (!match.getListing().getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de cette annonce");
        }

        if (accept) {
            match.setOwnerLikedBack(true);
            // Ne plus changer le statut de l'annonce ici - elle reste ACTIVE
            // Le statut changera seulement quand une réservation sera confirmée
        } else {
            // Si rejeté, supprimer le match
            matchRepository.delete(match);
            return null;
        }

        Match savedMatch = matchRepository.save(match);
        log.info("Match {} confirmé - L'annonce reste disponible pour d'autres matches", savedMatch.getId());
        return matchMapper.toDto(savedMatch);
    }

    /**
     * Récupérer les annonces non swipées pour un utilisateur
     */
    public List<ListingDto> getUnswipedListings(UUID userId) {
        return swipeRepository.findUnswipedListingsForUser(userId)
                .stream()
                .map(listingMapper::toDto)
                .toList();
    }

    /**
     * Récupérer tous les matches d'un utilisateur (confirmés et en attente)
     */
    public List<MatchDto> getMatchesForUser(UUID userId) {
        return matchRepository.findMatchesForUser(userId)
                .stream()
                .map(matchMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les matches confirmés d'un utilisateur
     */
    public List<MatchDto> getConfirmedMatchesForUser(UUID userId) {
        return matchRepository.findConfirmedMatchesForUser(userId)
                .stream()
                .map(matchMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les matches en attente pour un propriétaire (qui a reçu des likes)
     */
    public List<MatchDto> getPendingMatchesForOwner(UUID ownerId) {
        Profile owner = profileRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return matchRepository.findByListing_Owner(owner)
                .stream()
                .filter(match -> !match.isOwnerLikedBack())
                .map(matchMapper::toDto)
                .toList();
    }

    /**
     * Récupérer l'historique des swipes d'un utilisateur
     */
    public List<SwipeDto> getSwipeHistory(UUID userId) {
        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return swipeRepository.findBySwiper(user)
                .stream()
                .map(swipeMapper::toDto)
                .toList();
    }
}
