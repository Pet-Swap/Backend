package fr.petswap.backend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String avatarUrl;
    private String bio;
    private String role;
}
