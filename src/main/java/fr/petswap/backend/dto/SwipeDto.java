package fr.petswap.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class SwipeDto {
    private Integer id;
    private UUID swiperId;
    private String swiperUsername;
    private ListingDto listing;
    private String direction;
    private Instant swipedAt;
}
