package fr.petswap.backend.service;

import fr.petswap.backend.dao.jpa.Match;
import fr.petswap.backend.dao.jpa.Message;
import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dao.repository.MatchRepository;
import fr.petswap.backend.dao.repository.MessageRepository;
import fr.petswap.backend.dao.repository.ProfileRepository;
import fr.petswap.backend.dto.MessageDto;
import fr.petswap.backend.dto.SendMessageRequest;
import fr.petswap.backend.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;
    private final MessageMapper messageMapper;

    /**
     * Envoyer un message dans une conversation
     */
    public MessageDto sendMessage(UUID senderId, SendMessageRequest request) {
        log.info("Envoi d'un message par l'utilisateur {} pour le match {}", senderId, request.getMatchId());

        // Vérifier que le match existe et que l'utilisateur y participe
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        // Vérifier que l'utilisateur fait partie du match
        if (!match.getPetSitter().getId().equals(senderId) &&
            !match.getListing().getOwner().getId().equals(senderId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à envoyer des messages dans cette conversation");
        }

        // Vérifier que le match est confirmé
        if (!match.isOwnerLikedBack()) {
            throw new RuntimeException("Le match doit être confirmé pour envoyer des messages");
        }

        Profile sender = profileRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Message message = Message.builder()
                .match(match)
                .sender(sender)
                .content(request.getContent())
                .build();

        Message savedMessage = messageRepository.save(message);
        log.info("Message {} envoyé avec succès", savedMessage.getId());

        return messageMapper.toDto(savedMessage);
    }

    /**
     * Récupérer tous les messages d'un match
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesForMatch(Integer matchId, UUID userId) {
        log.info("Récupération des messages du match {} pour l'utilisateur {}", matchId, userId);

        // Vérifier que le match existe et que l'utilisateur y participe
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match non trouvé"));

        if (!match.getPetSitter().getId().equals(userId) &&
            !match.getListing().getOwner().getId().equals(userId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à voir cette conversation");
        }

        List<Message> messages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId);
        return messages.stream()
                .map(messageMapper::toDto)
                .toList();
    }

    /**
     * Récupérer les derniers messages de toutes les conversations d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getConversationsForUser(UUID userId) {
        log.info("Récupération des conversations pour l'utilisateur {}", userId);

        List<Message> messages = messageRepository.findConversationsForUser(userId);
        return messages.stream()
                .map(messageMapper::toDto)
                .toList();
    }
}
