package fr.petswap.backend.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Integer matchId;
    private String content;
}
