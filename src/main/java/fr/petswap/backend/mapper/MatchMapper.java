package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Match;
import fr.petswap.backend.dto.MatchDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ListingMapper.class, ProfileMapper.class})
public interface MatchMapper {

    @Mapping(source = "ownerLikedBack", target = "confirmed")
    MatchDto toDto(Match match);

    Match toEntity(MatchDto matchDto);
}
