package com.chat.controller;

import com.chat.dto.ConversationDto;
import com.chat.dto.CreateConversationRequest;
import com.chat.dto.ParticipantDto;
import com.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @PostMapping
    public ResponseEntity<ConversationDto> createConversation(
        @RequestBody CreateConversationRequest request,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        ConversationDto conversation = conversationService.createConversation(request, userId);
        return ResponseEntity.ok(conversation);
    }
    
    @GetMapping
    public ResponseEntity<List<ConversationDto>> getUserConversations(
        @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = getUserId(userDetails);
        List<ConversationDto> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable String id) {
        ConversationDto conversation = conversationService.getConversation(id);
        return ResponseEntity.ok(conversation);
    }
    
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantDto>> getParticipants(@PathVariable String id) {
        List<ParticipantDto> participants = conversationService.getParticipants(id);
        return ResponseEntity.ok(participants);
    }
    
    private String getUserId(UserDetails userDetails) {
        // In a real app, you would get the user ID from a custom UserDetails implementation
        // For now, we'll use the username as the user ID
        return userDetails.getUsername();
    }
}
