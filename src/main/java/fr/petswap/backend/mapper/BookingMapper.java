package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Booking;
import fr.petswap.backend.dto.BookingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MatchMapper.class, ReviewMapper.class})
public interface BookingMapper {

    @Mapping(source = "originalBooking.id", target = "originalBookingId")
    @Mapping(expression = "java(booking.isRebooking())", target = "isRebooking")
    BookingDto toDto(Booking booking);

    @Mapping(source = "originalBookingId", target = "originalBooking.id")
    @Mapping(expression = "java(bookingDto.getIsRebooking())", target = "isRebooking")
    Booking toEntity(BookingDto bookingDto);
}
