package fr.petswap.backend.service;
import fr.petswap.backend.config.Auth0JwtService;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.jpa.Profile.Role;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.auth.AuthRequest;
import fr.petswap.backend.dto.auth.AuthResponse;
import fr.petswap.backend.exception.InvalidCredentialsException;
import fr.petswap.backend.exception.UserAlreadyExistsException;
import fr.petswap.backend.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final ProfileRepository profileRepository;
    private final Auth0JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        log.info("Tentative de connexion pour {}", request.getUsername());
        var profile = profileRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    return new UserNotFoundException("Utilisateur non trouvé");
                });

        if (!passwordEncoder.matches(request.getPassword(), profile.getPassword())) {
            throw new InvalidCredentialsException("Identifiants invalides");
        }

        var token = jwtService.generateToken(profile);
        return new AuthResponse(token);
    }

    public AuthResponse register(AuthRequest request) {
        log.info("Inscription de {}", request.getUsername());
        var exists = profileRepository.existsByUsername(request.getUsername());
        if (exists) {
            throw new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé");
        }

        // Déterminer le rôle à partir de la requête, sinon OWNER par défaut
        Role role = Role.OWNER;
        if (request.getRole() != null) {
            try {
                role = Role.valueOf(request.getRole());
            } catch (IllegalArgumentException e) {
                log.warn("Rôle inconnu '{}', OWNER utilisé par défaut", request.getRole());
            }
        }

        // Déterminer l'avatar par défaut selon le rôle
        String avatarUrl = request.getAvatarUrl();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            switch (role) {
                case OWNER -> avatarUrl = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJsdWNpZGUgbHVjaWRlLWhlYXJ0LWljb24gbHVjaWRlLWhlYXJ0Ij48cGF0aCBkPSJNMiA5LjVhNS41IDUuNSAwIDAgMSA5LjU5MS0zLjY3Ni41Ni41NiAwIDAgMCAuODE4IDBBNS40OSA1LjQ5IDAgMCAxIDIyIDkuNWMwIDIuMjktMS41IDQtMyA1LjVsLTUuNDkyIDUuMzEzYTIgMiAwIDAgMS0zIC4wMTlMNSAxNWMtMS41LTEuNS0zLTMuMi0zLTUuNSIvPjwvc3ZnPg==";
                case PET_SITTER -> avatarUrl = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJsdWNpZGUgbHVjaWRlLXBhdy1wcmludC1pY29uIGx1Y2lkZS1wYXctcHJpbnQiPjxjaXJjbGUgY3g9IjExIiBjeT0iNCIgcj0iMiIvPjxjaXJjbGUgY3g9IjE4IiBjeT0iOCIgcj0iMiIvPjxjaXJjbGUgY3g9IjIwIiBjeT0iMTYiIHI9IjIiLz48cGF0aCBkPSJNOSAxMGE1IDUgMCAwIDEgNSA1djMuNWEzLjUgMy41IDAgMCAxLTYuODQgMS4wNDVRNi41MiAxNy40OCA0LjQ2IDE2Ljg0QTMuNSAzLjUgMCAwIDEgNS41IDEwWiIvPjwvc3ZnPg==";
                case BOTH -> avatarUrl = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJsdWNpZGUgbHVjaWRlLWhlYXJ0LWhhbmRzaGFrZS1pY29uIGx1Y2lkZS1oZWFydC1oYW5kc2hha2UiPjxwYXRoIGQ9Ik0xOS40MTQgMTQuNDE0QzIxIDEyLjgyOCAyMiAxMS41IDIyIDkuNWE1LjUgNS41IDAgMCAwLTkuNTkxLTMuNjc2LjYuNiAwIDAgMS0uODE4LjAwMUE1LjUgNS41IDAgMCAwIDIgOS41YzAgMi4zIDEuNSA0IDMgNS41bDUuNTM1IDUuMzYyYTIgMiAwIDAgMCAyLjg3OS4wNTIgMi4xMiAyLjEyIDAgMCAwLS4wMDQtMyAyLjEyNCAyLjEyNCAwIDEgMCAzLTMgMi4xMjQgMi4xMjQgMCAwIDAgMy4wMDQgMCAyIDIgMCAwIDAgMC0yLjgyOGwtMS44ODEtMS44ODJhMi40MSAyLjQxIDAgMCAwLTMuNDA5IDBsLTEuNzEgMS43MWEyIDIgMCAwIDEtMi44MjggMCAyIDIgMCAwIDEgMC0yLjgyOGwyLjgyMy0yLjc2MiIvPjwvc3ZnPg==";
                default -> avatarUrl = "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9ImN1cnJlbnRDb2xvciIgc3Ryb2tlLXdpZHRoPSIyIiBzdHJva2UtbGluZWNhcD0icm91bmQiIHN0cm9rZS1saW5lam9pbj0icm91bmQiIGNsYXNzPSJsdWNpZGUgbHVjaWRlLWhlYXJ0LWljb24gbHVjaWRlLWhlYXJ0Ij48cGF0aCBkPSJNMiA5LjVhNS41IDUuNSAwIDAgMSA5LjU5MS0zLjY3Ni41Ni41NiAwIDAgMCAuODE4IDBBNS40OSA1LjQ5IDAgMCAxIDIyIDkuNWMwIDIuMjktMS41IDQtMyA1LjVsLTUuNDkyIDUuMzEzYTIgMiAwIDAgMS0zIC4wMTlMNSAxNWMtMS41LTEuNS0zLTMuMi0zLTUuNSIvPjwvc3ZnPg==";
            }
        }

        var profile = Profile.builder()
                .id(UUID.randomUUID())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .avatarUrl(avatarUrl)
                .bio(request.getBio())
                .build();

        profileRepository.save(profile);
        var token = jwtService.generateToken(profile);
        log.info("Inscription réussie pour {}", request.getUsername());
        return new AuthResponse(token);
    }

    public boolean usernameExists(String username) {
        return profileRepository.existsByUsername(username);
    }
}