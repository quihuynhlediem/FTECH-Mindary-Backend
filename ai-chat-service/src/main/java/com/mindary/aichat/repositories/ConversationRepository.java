package com.mindary.aichat.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mindary.aichat.models.Conversation;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByUserIdOrderByLastMessageAtDesc(UUID userId);

    List<Conversation> findByFollowUpDueLessThanAndIsFollowedUpFalse(LocalDateTime dateTime);

    void deleteByIdAndUserId(String id, UUID userId);
}
