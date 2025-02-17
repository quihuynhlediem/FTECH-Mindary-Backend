package com.mindary.aichat.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mindary.aichat.models.ChatMessage;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByUserIdOrderByTimestampDesc(UUID userId);

    void deleteByUserId(UUID userId);

    void deleteByConversationId(String conversationId);

    List<ChatMessage> findByConversationIdOrderByTimestampDesc(String conversationId);

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    long countByConversationId(String conversationId);
}
