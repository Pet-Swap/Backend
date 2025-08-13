package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "swipes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Swipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "swipe_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "swiper_id", nullable = false)
    private Profile swiper;

    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private SwipeDirection direction;

    @Column(name = "swiped_at")
    @CreationTimestamp
    private Instant swipedAt;

    public enum SwipeDirection {
        LIKE, PASS
    }
}