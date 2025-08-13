package fr.petswap.backend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.petswap.backend.dao.jpa.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class Auth0JwtService {
    private final Algorithm algorithm = Algorithm.HMAC256("votre-secret-tres-secure");

    public String generateToken(Profile profile) {
        return JWT.create()
                .withSubject(profile.getUsername())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .withClaim("role", profile.getRole().name())
                .withClaim("userId", profile.getId().toString()) // Ajouter l'ID utilisateur
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token);
    }
}