package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Listing;
import fr.petswap.backend.dto.ListingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PetMapper.class})
public interface ListingMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.username", target = "ownerUsername")
    @Mapping(source = "owner.avatarUrl", target = "ownerAvatarUrl")
    @Mapping(source = "status", target = "status")
    ListingDto toDto(Listing listing);

    Listing toEntity(ListingDto listingDto);
}
