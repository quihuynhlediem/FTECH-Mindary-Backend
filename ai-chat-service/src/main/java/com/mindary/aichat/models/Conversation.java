package com.mindary.aichat.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;
    private UUID userId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private ConversationStatus status;
    private FollowUpType followUpType;
    private LocalDateTime followUpDue;
    private boolean isFollowedUp;
}
