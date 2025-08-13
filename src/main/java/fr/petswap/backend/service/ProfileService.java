package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.UpdateProfileRequest;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.mapper.ProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll()
                .stream()
                .map(profileMapper::toDto)
                .toList();
    }

    public ProfileDto getProfileById(UUID id) {
        return profileRepository.findById(id)
                .map(profileMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("Profil non trouvé"));
    }

    @Transactional
    public ProfileDto updateProfile(UUID id, UpdateProfileRequest request) {
        log.info("Mise à jour du profil {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Profil non trouvé"));

        // Vérifier si le nouveau nom d'utilisateur est déjà pris (si changé)
        if (request.getUsername() != null && !request.getUsername().equals(profile.getUsername())) {
            if (profileRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé");
            }
            profile.setUsername(request.getUsername());
        }

        // Mettre à jour les autres champs
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getRole() != null) {
            profile.setRole(Profile.Role.valueOf(request.getRole()));
        }

        Profile savedProfile = profileRepository.save(profile);
        log.info("Profil {} mis à jour avec succès", id);

        return profileMapper.toDto(savedProfile);
    }

    public ProfileDto getProfileByUsername(String username) {
        return profileRepository.findByUsername(username)
                .map(profileMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("Profil non trouvé"));
    }
}
