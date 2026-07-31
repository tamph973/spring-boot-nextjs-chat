package com.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_reactions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id", "emoji"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageReaction {
    
    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;
    
    @Column(columnDefinition = "CHAR(36)", nullable = false)
    private String messageId;
    
    @Column(columnDefinition = "CHAR(36)", nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 10)
    private String emoji;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
