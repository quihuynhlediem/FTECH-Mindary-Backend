package com.mindary.aichat.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;
    private UUID userId;
    private String conversationId;
    private String message;
    private String response;
    private LocalDateTime timestamp;
    private String diaryInsight;
    private Integer tokenCount;
    private MessageType type;
}
