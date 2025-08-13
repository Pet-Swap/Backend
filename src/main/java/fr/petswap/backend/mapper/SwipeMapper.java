package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Swipe;
import fr.petswap.backend.dto.SwipeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ListingMapper.class})
public interface SwipeMapper {

    @Mapping(source = "swiper.id", target = "swiperId")
    @Mapping(source = "swiper.username", target = "swiperUsername")
    SwipeDto toDto(Swipe swipe);

    Swipe toEntity(SwipeDto swipeDto);
}
