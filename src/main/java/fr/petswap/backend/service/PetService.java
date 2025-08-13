package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Pet;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.PetRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.CreatePetRequest;
import fr.petswap.backend.dto.PetDto;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final ProfileRepository profileRepository;
    private final PetMapper petMapper;

    @Transactional
    public PetDto createPet(UUID ownerId, CreatePetRequest request) {
        log.info("Création d'un pet pour l'utilisateur {}", ownerId);

        Profile owner = profileRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException("Propriétaire non trouvé"));

        Pet pet = Pet.builder()
                .owner(owner)
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .age(request.getAge())
                .photoUrl(request.getPhotoUrl())
                .specialNotes(request.getSpecialNotes())
                .build();

        Pet savedPet = petRepository.save(pet);
        log.info("Pet {} créé avec succès", savedPet.getId());

        return petMapper.toDto(savedPet);
    }

    public List<PetDto> getPetsByOwner(UUID ownerId) {
        return petRepository.findByOwnerId(ownerId)
                .stream()
                .map(petMapper::toDto)
                .toList();
    }

    public PetDto getPetById(Integer petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet non trouvé"));
        return petMapper.toDto(pet);
    }

    @Transactional
    public PetDto updatePet(Integer petId, CreatePetRequest request) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet non trouvé"));

        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setPhotoUrl(request.getPhotoUrl());
        pet.setSpecialNotes(request.getSpecialNotes());

        Pet savedPet = petRepository.save(pet);
        return petMapper.toDto(savedPet);
    }

    @Transactional
    public void deletePet(Integer petId) {
        petRepository.deleteById(petId);
        log.info("Pet {} supprimé", petId);
    }
}
