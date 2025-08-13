package fr.petswap.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    private Integer matchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String specialRequests; // Optionnel : demandes spéciales
}
