package fr.petswap.backend.dao.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "booking")
    private Set<Review> reviews;

    @Column(name = "is_rebooking")
    private boolean isRebooking = false;

    @ManyToOne
    @JoinColumn(name = "original_booking_id")
    private Booking originalBooking;

    @Column(name = "special_requests")
    private String specialRequests;

    public enum BookingStatus {
        PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
