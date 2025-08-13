package fr.petswap.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReviewStatusDto {
    private Integer bookingId;
    private UUID ownerId;
    private UUID petSitterId;
    private boolean ownerReviewedSitter;
    private boolean sitterReviewedOwner;
}
