package com.mindary.aichat.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mindary.aichat.models.ChatMessage;
import com.mindary.aichat.models.Conversation;
import com.mindary.aichat.models.ConversationStatus;
import com.mindary.aichat.models.FollowUpType;
import com.mindary.aichat.models.MessageType;
import com.mindary.aichat.repositories.ChatMessageRepository;
import com.mindary.aichat.repositories.ConversationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiService geminiService;

    public Conversation createConversation(UUID userId, String initialMessage) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(initialMessage));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    private String generateTitle(String message) {
        // enerate a concise title based on the first message with my man Gemini
        return geminiService.generateConversationTitle(message);
    }

    // Scheduled task to check for follow-ups (cai nay act as a truly therapists)
    @Scheduled(cron = "0 0 */1 * * *") // Run every hour for dev v1
    public void checkAndSendFollowUps() {
        LocalDateTime now = LocalDateTime.now();
        List<Conversation> conversationsNeedingFollowUp = conversationRepository
                .findByFollowUpDueLessThanAndIsFollowedUpFalse(now);

        for (Conversation conversation : conversationsNeedingFollowUp) {
            sendFollowUpMessage(conversation);
            conversation.setFollowedUp(true);
            conversationRepository.save(conversation);
        }
    }

    private void sendFollowUpMessage(Conversation conversation) {
        String followUpMessage = generateFollowUpMessage(conversation);
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversation.getId());
        message.setUserId(conversation.getUserId());
        message.setMessage(followUpMessage);
        message.setTimestamp(LocalDateTime.now());
        message.setType(MessageType.AI);
        chatMessageRepository.save(message);
    }

    private String generateFollowUpMessage(Conversation conversation) {
        switch (conversation.getFollowUpType()) {
            case HEALTH_CHECK:
                return "Hello! I've been thinking about you. Last time we spoke, you mentioned feeling unwell. How are you feeling today?";
            case ANXIETY_CHECK:
                return "Hi there! I remember you shared about experiencing anxiety. I wanted to check in - how are you managing today?";
            case SLEEP_CHECK:
                return "Good morning! Last time we talked, you mentioned having trouble sleeping. How did you sleep last night?";
            default:
                return "Hello! I wanted to check in and see how you're doing today.";
        }
    }

    public void scheduleFollowUp(Conversation conversation, FollowUpType type, LocalDateTime followUpTime) {
        conversation.setFollowUpType(type);
        conversation.setFollowUpDue(followUpTime);
        conversation.setFollowedUp(false);
        conversationRepository.save(conversation);
    }

    public List<Conversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
    }

    public Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }
}
