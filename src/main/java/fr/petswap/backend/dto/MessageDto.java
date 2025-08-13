package fr.petswap.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class MessageDto {
    private Integer id;
    private Integer matchId;
    private UUID senderId;
    private String senderUsername;
    private String content;
    private Instant sentAt;
}
