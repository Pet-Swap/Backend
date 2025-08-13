package fr.petswap.backend.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ListingDto {
    private Integer id;
    private UUID ownerId;
    private String ownerUsername;
    private String ownerAvatarUrl;
    private PetDto pet;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double pricePerDay;
    private String location;
    private String status;
}
