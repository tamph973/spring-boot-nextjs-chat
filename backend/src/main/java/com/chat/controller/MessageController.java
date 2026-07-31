package com.chat.controller;

import com.chat.dto.MessageDto;
import com.chat.dto.SendMessageRequest;
import com.chat.model.Message;
import com.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class MessageController {
    
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable String conversationId) {
        List<Message> messages = messageService.getMessagesByConversation(conversationId);
        List<MessageDto> messageDtos = messages.stream()
            .map(messageService::mapToDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(messageDtos);
    }
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request,
                           @AuthenticationPrincipal UserDetails userDetails) {
        
        Message message = Message.builder()
            .conversationId(request.getConversationId())
            .senderId(getUserId(userDetails))
            .content(request.getContent())
            .type(Message.MessageType.valueOf(request.getType()))
            .status(Message.MessageStatus.SENT)
            .build();
        
        Message savedMessage = messageService.createMessage(message);
        MessageDto messageDto = messageService.mapToDto(savedMessage);
        
        // Broadcast to the conversation topic
        messagingTemplate.convertAndSend(
            "/topic/conversation/" + request.getConversationId(),
            messageDto
        );
    }
    
    @MessageMapping("/chat.markRead")
    public void markMessageAsRead(@DestinationVariable String messageId) {
        messageService.updateMessageStatus(messageId, Message.MessageStatus.READ);
    }
    
    private String getUserId(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}
