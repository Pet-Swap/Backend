package fr.petswap.backend.controller;

import fr.petswap.backend.dto.ProfileDto;
import fr.petswap.backend.dto.auth.AuthRequest;
import fr.petswap.backend.dto.auth.AuthResponse;
import fr.petswap.backend.service.AuthService;
import fr.petswap.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDto> getCurrentUser(Authentication authentication) {
        // Récupérer l'ID utilisateur depuis le token JWT
        String username = authentication.getName(); // ou récupérer l'ID selon votre implémentation JWT

        // Appeler le ProfileService pour récupérer le profil
        ProfileDto profile = profileService.getProfileByUsername(username); // ou getProfileById si vous avez l'ID

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = authService.usernameExists(username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
