// ProfileDto.java
package fr.petswap.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProfileDto {
    private UUID id;
    private String username;
    private String avatarUrl;
    private String bio;
    private String role;
}
