package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Review;
import fr.petswap.backend.dto.ReviewDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "reviewer.username", target = "reviewerUsername")
    @Mapping(source = "reviewedUser.id", target = "reviewedUserId")
    @Mapping(source = "reviewedUser.username", target = "reviewedUserUsername")
    ReviewDto toDto(Review review);

    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewedUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Review toEntity(ReviewDto reviewDto);
}
