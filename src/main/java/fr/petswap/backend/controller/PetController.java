package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.CreatePetRequest;
import fr.petswap.backend.dto.PetDto;
import fr.petswap.backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping
    public ResponseEntity<PetDto> createPet(@RequestBody CreatePetRequest request, Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        PetDto pet = petService.createPet(currentUser.getId(), request);
        return ResponseEntity.ok(pet);
    }

    @GetMapping
    public ResponseEntity<List<PetDto>> getMyPets(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<PetDto> pets = petService.getPetsByOwner(currentUser.getId());
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetDto> getPetById(@PathVariable Integer petId) {
        PetDto pet = petService.getPetById(petId);
        return ResponseEntity.ok(pet);
    }

    @PutMapping("/{petId}")
    public ResponseEntity<PetDto> updatePet(@PathVariable Integer petId, @RequestBody CreatePetRequest request) {
        PetDto updatedPet = petService.updatePet(petId, request);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable Integer petId) {
        petService.deletePet(petId);
        return ResponseEntity.ok().build();
    }
}
