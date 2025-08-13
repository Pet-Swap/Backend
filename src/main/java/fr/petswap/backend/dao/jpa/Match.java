package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Set;

import java.time.Instant;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "match_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne
    @JoinColumn(name = "pet_sitter_id", nullable = false)
    private Profile petSitter;

    @Column(name = "owner_liked_back")
    private boolean ownerLikedBack = false;

    @Column(name = "matched_at")
    @CreationTimestamp
    private Instant matchedAt;

    @OneToMany(mappedBy = "match")
    private Set<Message> messages;

    @OneToMany(mappedBy = "match")
    private Set<Booking> bookings;
}