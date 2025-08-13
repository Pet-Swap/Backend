package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.CreateListingRequest;
import fr.petswap.backend.dto.ListingDto;
import fr.petswap.backend.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<ListingDto> createListing(@RequestBody CreateListingRequest request, Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        ListingDto listing = listingService.createListing(currentUser.getId(), request);
        return ResponseEntity.ok(listing);
    }

    @GetMapping("/my-listings")
    public ResponseEntity<List<ListingDto>> getMyListings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<ListingDto> listings = listingService.getListingsByOwner(currentUser.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/browse")
    public ResponseEntity<List<ListingDto>> browseListings(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<ListingDto> listings = listingService.getActiveListings(currentUser.getId());
        return ResponseEntity.ok(listings);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingDto> getListingById(@PathVariable Integer listingId) {
        ListingDto listing = listingService.getListingById(listingId);
        return ResponseEntity.ok(listing);
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingDto> updateListing(@PathVariable Integer listingId, @RequestBody CreateListingRequest request) {
        ListingDto updatedListing = listingService.updateListing(listingId, request);
        return ResponseEntity.ok(updatedListing);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable Integer listingId) {
        listingService.deleteListing(listingId);
        return ResponseEntity.ok().build();
    }
}
