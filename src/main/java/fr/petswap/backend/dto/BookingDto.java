package fr.petswap.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class BookingDto {
    private Integer id;
    private MatchDto match;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String status;
    private Instant createdAt;
    private List<ReviewDto> reviews;
    private Boolean isRebooking;
    private Integer originalBookingId;
    private String specialRequests;
}
