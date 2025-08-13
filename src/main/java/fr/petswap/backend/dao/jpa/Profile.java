package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;


    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    private String bio;

    private float rating = 0;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "owner")
    private Set<Pet> pets;

    @OneToMany(mappedBy = "owner")
    private Set<Listing> listings;

    @OneToMany(mappedBy = "swiper")
    private Set<Swipe> swipes;

    @OneToMany(mappedBy = "petSitter")
    private Set<Match> matchesAsPetSitter;

    @OneToMany(mappedBy = "sender")
    private Set<Message> messages;

    @OneToMany(mappedBy = "reviewer")
    private Set<Review> reviews;

    public enum Role {
        OWNER, PET_SITTER, BOTH
    }
}