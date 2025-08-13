package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dao.jpa.Pet;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.ListingRepository;
import fr.petswap.backend.dao.repository.PetRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.CreateListingRequest;
import fr.petswap.backend.dto.ListingDto;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.mapper.ListingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final PetRepository petRepository;
    private final ProfileRepository profileRepository;
    private final ListingMapper listingMapper;

    @Transactional
    public ListingDto createListing(UUID ownerId, CreateListingRequest request) {
        log.info("Création d'une annonce pour l'utilisateur {}", ownerId);

        Profile owner = profileRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Propriétaire non trouvé"));

        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new RuntimeException("Pet non trouvé"));

        // Vérifier que le pet appartient bien au propriétaire
        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de ce pet");
        }

        Listing listing = Listing.builder()
                .owner(owner)
                .pet(pet)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .pricePerDay(request.getPricePerDay())
                .location(request.getLocation())
                .status(Listing.ListingStatus.ACTIVE)
                .build();

        Listing savedListing = listingRepository.save(listing);
        log.info("Annonce {} créée avec succès", savedListing.getId());

        return listingMapper.toDto(savedListing);
    }

    public List<ListingDto> getListingsByOwner(UUID ownerId) {
        return listingRepository.findByOwnerId(ownerId)
                .stream()
                .map(listingMapper::toDto)
                .toList();
    }

    public List<ListingDto> getActiveListings(UUID userId) {
        return listingRepository.findActiveListingsExcludingUser(userId)
                .stream()
                .map(listingMapper::toDto)
                .toList();
    }

    public ListingDto getListingById(Integer listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));
        return listingMapper.toDto(listing);
    }

    @Transactional
    public ListingDto updateListing(Integer listingId, CreateListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Annonce non trouvée"));

        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setStartDate(request.getStartDate());
        listing.setEndDate(request.getEndDate());
        listing.setPricePerDay(request.getPricePerDay());
        listing.setLocation(request.getLocation());

        Listing savedListing = listingRepository.save(listing);
        return listingMapper.toDto(savedListing);
    }

    @Transactional
    public void deleteListing(Integer listingId) {
        listingRepository.deleteById(listingId);
        log.info("Annonce {} supprimée", listingId);
    }
}
