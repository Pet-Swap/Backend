package fr.petswap.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.petswap.backend.config.CustomExceptionHandler;
import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.UpdateProfileRequest;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    private ObjectMapper objectMapper;
    private ProfileDto profileDto;
    private UpdateProfileRequest updateRequest;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        profileId = UUID.randomUUID();

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
    void getProfileById_ShouldReturnProfile_WhenProfileExists() throws Exception {
        // Given
        when(profileService.getProfileById(profileId)).thenReturn(profileDto);

        // When & Then
        mockMvc.perform(get("/api/profiles/{id}", profileId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.bio").value("Test bio"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void getProfileById_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        // Given
        when(profileService.getProfileById(profileId))
                .thenThrow(new UserNotFoundException("Profil non trouvé"));

        // When & Then
        mockMvc.perform(get("/api/profiles/{id}", profileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProfiles_ShouldReturnListOfProfiles() throws Exception {
        // Given
        ProfileDto profile2 = new ProfileDto();
        profile2.setId(UUID.randomUUID());
        profile2.setUsername("testuser2");
        profile2.setRole("PET_SITTER");

        List<ProfileDto> profiles = Arrays.asList(profileDto, profile2);
        when(profileService.getAllProfiles()).thenReturn(profiles);

        // When & Then
        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[1].username").value("testuser2"));
    }

    @Test
    void getAllProfiles_ShouldReturnEmptyList_WhenNoProfiles() throws Exception {
        // Given
        when(profileService.getAllProfiles()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/profiles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfile_WhenValidRequest() throws Exception {
        // Given
        ProfileDto updatedProfile = new ProfileDto();
        updatedProfile.setId(profileId);
        updatedProfile.setUsername("updateduser");
        updatedProfile.setAvatarUrl("https://example.com/new-avatar.jpg");
        updatedProfile.setBio("Updated bio");
        updatedProfile.setRole("PET_SITTER");

        when(profileService.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/new-avatar.jpg"))
                .andExpect(jsonPath("$.bio").value("Updated bio"))
                .andExpect(jsonPath("$.role").value("PET_SITTER"));
    }

    @Test
    void updateProfile_ShouldReturnNotFound_WhenProfileDoesNotExist() throws Exception {
        // Given
        when(profileService.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
                .thenThrow(new UserNotFoundException("Profil non trouvé"));

        // When & Then
        mockMvc.perform(put("/api/profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProfile_ShouldReturnConflict_WhenUsernameAlreadyExists() throws Exception {
        // Given
        when(profileService.updateProfile(eq(profileId), any(UpdateProfileRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé"));

        // When & Then
        mockMvc.perform(put("/api/profiles/{id}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }

}
