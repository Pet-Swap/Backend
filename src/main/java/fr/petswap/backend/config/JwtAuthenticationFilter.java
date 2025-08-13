// JwtAuthenticationFilter.java
package fr.petswap.backend.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.ProfileRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final Auth0JwtService jwtService;
    private final ProfileRepository profileRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        DecodedJWT decodedJWT = jwtService.verifyToken(token);
        String username = decodedJWT.getSubject();

        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + profile.getRole().name());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(profile, null, List.of(authority));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
