package com.chat.service;

import com.chat.dto.MessageDto;
import com.chat.model.Message;
import com.chat.model.User;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }
    
    public List<Message> getMessagesByConversation(String conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
    
    public MessageDto mapToDto(Message message) {
        User sender = userRepository.findById(message.getSenderId()).orElse(null);
        
        return MessageDto.builder()
            .id(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .senderName(sender != null ? sender.getUsername() : "Unknown")
            .senderAvatar(sender != null ? sender.getAvatarUrl() : null)
            .content(message.getContent())
            .type(message.getType().name())
            .status(message.getStatus().name())
            .createdAt(message.getCreatedAt())
            .build();
    }
    
    @Transactional
    public void updateMessageStatus(String messageId, Message.MessageStatus status) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        message.setStatus(status);
        messageRepository.save(message);
    }
}
