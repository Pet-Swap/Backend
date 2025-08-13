package fr.petswap.backend.controller;

import fr.petswap.backend.dao.jpa.Profile;
import fr.petswap.backend.dto.MessageDto;
import fr.petswap.backend.dto.SendMessageRequest;
import fr.petswap.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(
            @RequestBody SendMessageRequest request,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        MessageDto message = messageService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MessageDto>> getMessagesForMatch(
            @PathVariable Integer matchId,
            Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<MessageDto> messages = messageService.getMessagesForMatch(matchId, currentUser.getId());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<MessageDto>> getMyConversations(Authentication authentication) {
        Profile currentUser = (Profile) authentication.getPrincipal();
        List<MessageDto> conversations = messageService.getConversationsForUser(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }
}
