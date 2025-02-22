package com.mindary.aichat.services;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mindary.aichat.models.EmbeddedMessage;
import com.mindary.aichat.repositories.EmbeddedMessageRepository;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final EmbeddedMessageRepository embeddedMessageRepository;
    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public EmbeddedMessage createEmbedding(String messageId, String conversationId, String content) {
        try {
            Embedding embedding = embeddingModel.embed(content).content();

            EmbeddedMessage embeddedMessage = new EmbeddedMessage();
            embeddedMessage.setMessageId(messageId);
            embeddedMessage.setConversationId(conversationId);
            embeddedMessage.setContent(content);
            List<Float> vector = embedding.vectorAsList();
            float[] floatArray = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                floatArray[i] = vector.get(i);
            }
            embeddedMessage.setEmbedding(floatArray);
            embeddedMessage.setTimestamp(LocalDateTime.now());

            return embeddedMessageRepository.save(embeddedMessage);
        } catch (Exception e) {
            log.error("Error creating embedding for message {}: {}", messageId, e.getMessage());
            return null;
        }
    }

    public List<String> findSimilarMessages(String conversationId, String query, int limit) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            List<EmbeddedMessage> conversationMessages
                    = embeddedMessageRepository.findEmbeddedMessagesByConversationId(conversationId);

            return conversationMessages.stream()
                    .map(msg -> {
                        float similarity = cosineSimilarity(
                                convertToFloatArray(queryEmbedding.vectorAsList()),
                                msg.getEmbedding()
                        );
                        return new AbstractMap.SimpleEntry<>(msg.getContent(), similarity);
                    })
                    .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error finding similar messages: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private float cosineSimilarity(float[] vectorA, float[] vectorB) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private float[] convertToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
