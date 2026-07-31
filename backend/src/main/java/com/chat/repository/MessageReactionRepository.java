package com.chat.repository;

import com.chat.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {
    List<MessageReaction> findByMessageId(String messageId);
    void deleteByMessageIdAndUserId(String messageId, String userId);
}
