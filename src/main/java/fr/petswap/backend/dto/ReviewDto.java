package fr.petswap.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class ReviewDto {
    private Integer id;
    private Integer bookingId;
    private UUID reviewerId;
    private String reviewerUsername;
    private UUID reviewedUserId;
    private String reviewedUserUsername;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
