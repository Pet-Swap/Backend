package fr.petswap.backend.dto;

import lombok.Data;

@Data
public class CreatePetRequest {
    private String name;
    private String species;
    private String breed;
    private Integer age;
    private String photoUrl;
    private String specialNotes;
}
