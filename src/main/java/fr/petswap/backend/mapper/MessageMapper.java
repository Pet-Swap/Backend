package fr.petswap.backend.mapper;

import fr.petswap.backend.dao.jpa.Message;
import fr.petswap.backend.dto.MessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "match.id", target = "matchId")
    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.username", target = "senderUsername")
    MessageDto toDto(Message message);

    @Mapping(target = "match", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    Message toEntity(MessageDto messageDto);
}
