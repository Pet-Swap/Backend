package fr.petswap.backend.dao.repository;

import fr.petswap.backend.dao.jpa.Message;
import fr.petswap.backend.dao.jpa.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByMatchOrderBySentAtAsc(Match match);

    @Query("SELECT m FROM Message m WHERE m.match.id = :matchId ORDER BY m.sentAt ASC")
    List<Message> findByMatchIdOrderBySentAtAsc(@Param("matchId") Integer matchId);

    @Query("SELECT DISTINCT m FROM Message m WHERE m.match.listing.owner.id = :userId OR m.match.petSitter.id = :userId ORDER BY m.sentAt DESC")
    List<Message> findConversationsForUser(@Param("userId") UUID userId);
}
