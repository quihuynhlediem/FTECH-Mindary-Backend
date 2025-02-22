package com.mindary.aichat.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Document(collection = "embedded_messages")
public class EmbeddedMessage {

    @Id
    private String id;
    private String messageId;
    private String conversationId;
    private String content;
    private float[] embedding;
    private LocalDateTime timestamp;
}
