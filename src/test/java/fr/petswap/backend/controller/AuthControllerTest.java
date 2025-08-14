package fr.petswap.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.petswap.backend.config.CustomExceptionHandler;
import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.auth.AuthRequest;
import fr.petswap.backend.dto.auth.AuthResponse;
import fr.petswap.backend.exception.InvalidCredentialsException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.exception.UserNotFoundException;
import fr.petswap.backend.service.AuthService;
import fr.petswap.backend.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private ProfileService profileService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private AuthRequest authRequest;
    private AuthResponse authResponse;
    private ProfileDto profileDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");

        authResponse = new AuthResponse("jwt-token-123");

        profileDto = new ProfileDto();
        profileDto.setId(UUID.randomUUID());
        profileDto.setUsername("testuser");
        profileDto.setRole("OWNER");
        profileDto.setBio("Test bio");
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void login_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class)))
                .thenThrow(new InvalidCredentialsException("Identifiants invalides"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_ShouldReturnToken_WhenValidRequest() throws Exception {
        // Given
        when(authService.register(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void register_ShouldReturnConflict_WhenUserAlreadyExists() throws Exception {
        // Given
        when(authService.register(any(AuthRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void logout_ShouldReturnSuccessMessage() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Déconnexion réussie"));
    }

    @Test
    void getCurrentUser_ShouldReturnProfile_WhenAuthenticated() throws Exception {
        // Given
        when(authentication.getName()).thenReturn("testuser");
        when(profileService.getProfileByUsername("testuser")).thenReturn(profileDto);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(profileDto.getId().toString()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void getCurrentUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Given
        when(authentication.getName()).thenReturn("nonexistent");
        when(profileService.getProfileByUsername("nonexistent"))
                .thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isNotFound());
    }
}
