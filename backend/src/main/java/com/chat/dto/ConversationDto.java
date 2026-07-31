package com.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private String id;
    private String type;
    private String name;
    private String avatarUrl;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ParticipantDto> participants;
    private MessageDto lastMessage;
}
