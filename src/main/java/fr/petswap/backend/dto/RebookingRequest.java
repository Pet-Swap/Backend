package fr.petswap.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RebookingRequest {
    private Integer matchId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String specialRequests;
    private boolean isRebooking = true;
}

