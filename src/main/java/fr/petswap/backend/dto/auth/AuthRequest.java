package fr.petswap.backend.dto.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
    private String role; // Ajout du champ role pour l'inscription
    private String bio; // Ajout du champ bio pour l'inscription
    private String avatarUrl; // Ajout du champ avatarUrl pour l'inscription
}
