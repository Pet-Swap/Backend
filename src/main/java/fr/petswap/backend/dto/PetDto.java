package fr.petswap.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PetDto {
    private Integer id;
    private UUID ownerId;
    private String ownerUsername;
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String photoUrl;
    private String specialNotes;
}
