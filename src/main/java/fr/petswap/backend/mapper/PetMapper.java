package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Pet;
import fr.petswap.backend.dto.PetDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.username", target = "ownerUsername")
    PetDto toDto(Pet pet);

    Pet toEntity(PetDto petDto);
}
