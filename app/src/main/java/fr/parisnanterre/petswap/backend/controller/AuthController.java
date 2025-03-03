package fr.parisnanterre.petswap.backend.controller;

import fr.parisnanterre.petswap.backend.config.JwtUtils;
import fr.parisnanterre.petswap.backend.entity.Role;
import fr.parisnanterre.petswap.backend.entity.User;
import fr.parisnanterre.petswap.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody  User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Username is already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            if (authentication.isAuthenticated()){
                Map<String, Object> authData = new HashMap<>();
                authData.put("token", jwtUtils.generateToken(user.getEmail()));
                authData.put("type", "Bearer");
                return ResponseEntity.ok(authData);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        } catch (AuthenticationException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
}
