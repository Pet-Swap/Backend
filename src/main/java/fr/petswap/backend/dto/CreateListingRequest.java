package fr.petswap.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateListingRequest {
    private Integer petId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double pricePerDay;
    private String location;
}
