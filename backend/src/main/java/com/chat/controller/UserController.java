package com.chat.controller;

import com.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        var user = userService.getUser(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.getUser(getUserId(userDetails));
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
    
    @MessageMapping("/user.online")
    public void handleUserOnline(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        userService.setUserOnline(userId);
        
        // Broadcast online status
        messagingTemplate.convertAndSend("/topic/user/status/" + userId, 
            new UserStatusPayload(userId, "ONLINE"));
    }
    
    @MessageMapping("/user.offline")
    public void handleUserOffline(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = getUserId(userDetails);
        userService.setUserOffline(userId);
        
        // Broadcast offline status
        messagingTemplate.convertAndSend("/topic/user/status/" + userId, 
            new UserStatusPayload(userId, "OFFLINE"));
    }
    
    private String getUserId(UserDetails userDetails) {
        return userDetails.getUsername();
    }
    
    public static class UserStatusPayload {
        public String userId;
        public String status;
        
        public UserStatusPayload(String userId, String status) {
            this.userId = userId;
            this.status = status;
        }
    }
}
