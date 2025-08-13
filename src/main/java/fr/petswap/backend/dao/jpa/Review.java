package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Profile reviewer;

    @ManyToOne
    @JoinColumn(name = "reviewed_user_id", nullable = false)
    private Profile reviewedUser;

    @Column(nullable = false)
    private Integer rating;

    private String comment;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;
}