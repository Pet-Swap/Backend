package fr.petswap.backend.service;

import fr.petswap.backend.config.Auth0JwtService;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.auth.AuthRequest;
import fr.petswap.backend.dto.auth.AuthResponse;
import fr.petswap.backend.exception.InvalidCredentialsException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private Auth0JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private Profile profile;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
        authRequest.setRole("OWNER");

        profile = new Profile();
        profile.setId(UUID.randomUUID());
        profile.setUsername("testuser");
        profile.setPassword("encodedPassword");
        profile.setRole(Profile.Role.OWNER);
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() {
        // Given
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(profile)).thenReturn("jwt-token-123");

        // When
        AuthResponse result = authService.login(authRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-123");
        verify(jwtService).generateToken(profile);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Utilisateur non trouvé");
    }

    @Test
    void login_ShouldThrowException_WhenInvalidPassword() {
        // Given
        when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(authRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Identifiants invalides");
    }

    @Test
    void register_ShouldReturnToken_WhenValidRequest() {
        // Given
        when(profileRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(jwtService.generateToken(any(Profile.class))).thenReturn("jwt-token-123");

        // When
        AuthResponse result = authService.register(authRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-123");
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        // Given
        when(profileRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(authRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Nom d'utilisateur déjà utilisé");

        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void register_ShouldUseOwnerRole_WhenNoRoleProvided() {
        // Given
        authRequest.setRole(null);
        when(profileRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(jwtService.generateToken(any(Profile.class))).thenReturn("jwt-token-123");

        // When
        AuthResponse result = authService.register(authRequest);

        // Then
        assertThat(result).isNotNull();
        verify(profileRepository).save(argThat(savedProfile ->
            savedProfile.getRole() == Profile.Role.OWNER));
    }

    @Test
    void register_ShouldUsePetSitterRole_WhenSpecified() {
        // Given
        authRequest.setRole("PET_SITTER");
        when(profileRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(jwtService.generateToken(any(Profile.class))).thenReturn("jwt-token-123");

        // When
        AuthResponse result = authService.register(authRequest);

        // Then
        assertThat(result).isNotNull();
        verify(profileRepository).save(argThat(savedProfile ->
            savedProfile.getRole() == Profile.Role.PET_SITTER));
    }
}
