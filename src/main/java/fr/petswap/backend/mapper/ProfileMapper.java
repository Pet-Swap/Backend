package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.ProfileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {

    @Mapping(source = "role", target = "role")
    ProfileDto toDto(Profile profile);
}
