package com.chat.repository;

import com.chat.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, String> {
    List<ConversationParticipant> findByUserId(String userId);
    List<ConversationParticipant> findByConversationId(String conversationId);
    Optional<ConversationParticipant> findByConversationIdAndUserId(String conversationId, String userId);
    void deleteByConversationIdAndUserId(String conversationId, String userId);
}
