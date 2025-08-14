package fr.petswap.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateReviewRequest {
    @NotNull(message = "L'ID de la réservation est requis")
    private Integer bookingId;

    @NotNull(message = "La note est requise")
    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    private Integer rating;

    private String comment;
}
