package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Profile owner;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 100)
    private String title;

    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price_per_day")
    private Double pricePerDay;

    private String location;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "listing")
    private Set<Swipe> swipes;

    @OneToMany(mappedBy = "listing")
    private Set<Match> matches;

    public enum ListingStatus {
        ACTIVE,      // Annonce disponible, peut avoir des matches
        RESERVED,    // Au moins une réservation confirmée pour certaines dates
        COMPLETED,   // Service terminé
        INACTIVE     // Annonce désactivée par le propriétaire
    }
}