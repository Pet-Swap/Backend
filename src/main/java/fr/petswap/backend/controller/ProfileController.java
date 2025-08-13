package fr.petswap.backend.controller;

import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.UpdateProfileRequest;
import fr.petswap.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDto> getProfileById(@PathVariable UUID id) {
        ProfileDto profile = profileService.getProfileById(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public List<ProfileDto> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileDto> updateProfile(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
        ProfileDto updatedProfile = profileService.updateProfile(id, request);
        return ResponseEntity.ok(updatedProfile);
    }
}