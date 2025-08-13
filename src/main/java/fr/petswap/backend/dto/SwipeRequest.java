package fr.petswap.backend.dto;

import lombok.Data;

@Data
public class SwipeRequest {
    private Integer listingId;
    private String direction; // "LIKE" ou "PASS"
}
