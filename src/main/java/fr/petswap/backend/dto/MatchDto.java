package fr.petswap.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class MatchDto {
    private Integer id;
    private ListingDto listing;
    private ProfileDto petSitter;
    private boolean ownerLikedBack;
    private Instant matchedAt;
    private boolean isConfirmed;
}
