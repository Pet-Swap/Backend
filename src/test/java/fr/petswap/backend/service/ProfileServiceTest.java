package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.UpdateProfileRequest;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.mapper.ProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile;
    private ProfileDto profileDto;
    private UpdateProfileRequest updateRequest;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();

        profile = Profile.builder()
                .id(profileId)
                .username("testuser")
                .password("password")
                .avatarUrl("https://example.com/avatar.jpg")
                .bio("Test bio")
                .role(Profile.Role.OWNER)
                .rating(4.5f)
                .build();

        profileDto = new ProfileDto();
        profileDto.setId(profileId);
        profileDto.setUsername("testuser");
        profileDto.setAvatarUrl("https://example.com/avatar.jpg");
        profileDto.setBio("Test bio");
        profileDto.setRole("OWNER");

        updateRequest = new UpdateProfileRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setAvatarUrl("https://example.com/new-avatar.jpg");
        updateRequest.setBio("Updated bio");
        updateRequest.setRole("PET_SITTER");
    }

    @Test
    void getAllProfiles_ShouldReturnListOfProfileDtos() {
        // Given
        Profile profile2 = Profile.builder()
                .id(UUID.randomUUID())
                .username("testuser2")
                .role(Profile.Role.PET_SITTER)
                .build();

        ProfileDto profileDto2 = new ProfileDto();
        profileDto2.setId(profile2.getId());
        profileDto2.setUsername("testuser2");
        profileDto2.setRole("PET_SITTER");

        List<Profile> profiles = Arrays.asList(profile, profile2);
        when(profileRepository.findAll()).thenReturn(profiles);
        when(profileMapper.toDto(profile)).thenReturn(profileDto);
        when(profileMapper.toDto(profile2)).thenReturn(profileDto2);

        // When
        List<ProfileDto> result = profileService.getAllProfiles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
        verify(profileRepository).findAll();
        verify(profileMapper, times(2)).toDto(any(Profile.class));
    }

    @Test
    void getProfileById_ShouldReturnProfileDto_WhenProfileExists() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileMapper.toDto(profile)).thenReturn(profileDto);

        // When
        ProfileDto result = profileService.getProfileById(profileId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(profileId);
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(profileRepository).findById(profileId);
        verify(profileMapper).toDto(profile);
    }

    @Test
    void getProfileById_ShouldThrowUserNotFoundException_WhenProfileDoesNotExist() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> profileService.getProfileById(profileId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Profil non trouvé");
        verify(profileRepository).findById(profileId);
        verifyNoInteractions(profileMapper);
    }

    @Test
    void updateProfile_ShouldUpdateAndReturnProfileDto_WhenValidRequest() {
        // Given
        Profile updatedProfile = Profile.builder()
                .id(profileId)
                .username("updateduser")
                .password("password")
                .avatarUrl("https://example.com/new-avatar.jpg")
                .bio("Updated bio")
                .role(Profile.Role.PET_SITTER)
                .build();

        ProfileDto updatedProfileDto = new ProfileDto();
        updatedProfileDto.setId(profileId);
        updatedProfileDto.setUsername("updateduser");
        updatedProfileDto.setAvatarUrl("https://example.com/new-avatar.jpg");
        updatedProfileDto.setBio("Updated bio");
        updatedProfileDto.setRole("PET_SITTER");

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByUsername("updateduser")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(profileMapper.toDto(updatedProfile)).thenReturn(updatedProfileDto);

        // When
        ProfileDto result = profileService.updateProfile(profileId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updateduser");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg");
        assertThat(result.getBio()).isEqualTo("Updated bio");
        assertThat(result.getRole()).isEqualTo("PET_SITTER");

        verify(profileRepository).findById(profileId);
        verify(profileRepository).existsByUsername("updateduser");
        verify(profileRepository).save(any(Profile.class));
        verify(profileMapper).toDto(updatedProfile);
    }

    @Test
    void updateProfile_ShouldThrowUserNotFoundException_WhenProfileDoesNotExist() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> profileService.updateProfile(profileId, updateRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Profil non trouvé");
        verify(profileRepository).findById(profileId);
        verifyNoMoreInteractions(profileRepository);
        verifyNoInteractions(profileMapper);
    }

    @Test
    void updateProfile_ShouldThrowUserAlreadyExistsException_WhenUsernameAlreadyExists() {
        // Given
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.existsByUsername("updateduser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> profileService.updateProfile(profileId, updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Nom d'utilisateur déjà utilisé");
        verify(profileRepository).findById(profileId);
        verify(profileRepository).existsByUsername("updateduser");
        verify(profileRepository, never()).save(any());
        verifyNoInteractions(profileMapper);
    }

    @Test
    void updateProfile_ShouldNotCheckUsername_WhenUsernameIsNull() {
        // Given
        updateRequest.setUsername(null);
        Profile updatedProfile = Profile.builder()
                .id(profileId)
                .username("testuser") // Username unchanged
                .password("password")
                .avatarUrl("https://example.com/new-avatar.jpg")
                .bio("Updated bio")
                .role(Profile.Role.PET_SITTER)
                .build();

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(profileMapper.toDto(updatedProfile)).thenReturn(profileDto);

        // When
        profileService.updateProfile(profileId, updateRequest);

        // Then
        verify(profileRepository).findById(profileId);
        verify(profileRepository, never()).existsByUsername(any());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void updateProfile_ShouldNotCheckUsername_WhenUsernameIsUnchanged() {
        // Given
        updateRequest.setUsername("testuser"); // Same username
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(profileMapper.toDto(profile)).thenReturn(profileDto);

        // When
        profileService.updateProfile(profileId, updateRequest);

        // Then
        verify(profileRepository).findById(profileId);
        verify(profileRepository, never()).existsByUsername(any());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void updateProfile_ShouldUpdateOnlyProvidedFields() {
        // Given
        UpdateProfileRequest partialRequest = new UpdateProfileRequest();
        partialRequest.setBio("Only bio updated");
        // Other fields are null

        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(profileMapper.toDto(profile)).thenReturn(profileDto);

        // When
        profileService.updateProfile(profileId, partialRequest);

        // Then
        verify(profileRepository).findById(profileId);
        verify(profileRepository).save(any(Profile.class));
        verify(profileMapper).toDto(profile);
    }

    @Test
    void getProfileByUsername_ShouldReturnProfileDto_WhenProfileExists() {
        // Given
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(profile));
        when(profileMapper.toDto(profile)).thenReturn(profileDto);

        // When
        ProfileDto result = profileService.getProfileByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        verify(profileRepository).findByUsername(username);
        verify(profileMapper).toDto(profile);
    }

    @Test
    void getProfileByUsername_ShouldThrowUserNotFoundException_WhenProfileDoesNotExist() {
        // Given
        String username = "nonexistentuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> profileService.getProfileByUsername(username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Profil non trouvé");
        verify(profileRepository).findByUsername(username);
        verifyNoInteractions(profileMapper);
    }
}
