package com.chat.service;

import com.chat.dto.*;
import com.chat.model.*;
import com.chat.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ConversationDto createConversation(CreateConversationRequest request, String createdBy) {
        Conversation.ConversationType type = Conversation.ConversationType.valueOf(request.getType());
        
        Conversation conversation = Conversation.builder()
            .type(type)
            .name(request.getName())
            .avatarUrl(request.getAvatarUrl())
            .createdBy(createdBy)
            .build();
        
        Conversation saved = conversationRepository.save(conversation);
        
        // Add participants
        if (request.getParticipantIds() != null) {
            for (String userId : request.getParticipantIds()) {
                ConversationParticipant participant = ConversationParticipant.builder()
                    .conversationId(saved.getId())
                    .userId(userId)
                    .role(userId.equals(createdBy) ? ConversationParticipant.Role.ADMIN : ConversationParticipant.Role.MEMBER)
                    .build();
                participantRepository.save(participant);
            }
        }
        
        return mapToDto(saved);
    }
    
    public List<ConversationDto> getUserConversations(String userId) {
        List<ConversationParticipant> participants = participantRepository.findByUserId(userId);
        
        return participants.stream()
            .map(p -> conversationRepository.findById(p.getConversationId()).orElse(null))
            .filter(c -> c != null)
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }
    
    public ConversationDto getConversation(String id) {
        Conversation conversation = conversationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        return mapToDto(conversation);
    }
    
    public List<ParticipantDto> getParticipants(String conversationId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversationId);
        
        return participants.stream()
            .map(p -> {
                User user = userRepository.findById(p.getUserId()).orElse(null);
                if (user == null) return null;
                return ParticipantDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .avatarUrl(user.getAvatarUrl())
                    .role(p.getRole().name())
                    .status(user.getStatus().name())
                    .build();
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());
    }
    
    private ConversationDto mapToDto(Conversation conversation) {
        return ConversationDto.builder()
            .id(conversation.getId())
            .type(conversation.getType().name())
            .name(conversation.getName())
            .avatarUrl(conversation.getAvatarUrl())
            .createdBy(conversation.getCreatedBy())
            .createdAt(conversation.getCreatedAt())
            .build();
    }
}
