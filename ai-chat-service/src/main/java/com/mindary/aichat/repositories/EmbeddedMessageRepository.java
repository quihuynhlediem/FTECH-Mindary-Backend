package com.mindary.aichat.repositories;

import com.mindary.aichat.models.EmbeddedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmbeddedMessageRepository extends MongoRepository<EmbeddedMessage, String> {

    List<EmbeddedMessage> findByConversationId(String conversationId);

    @Query("{ 'conversationId': ?0, 'embedding': { $exists: true } }")
    List<EmbeddedMessage> findEmbeddedMessagesByConversationId(String conversationId);
}
