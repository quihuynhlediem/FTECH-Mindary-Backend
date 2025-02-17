package com.mindary.aichat.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mindary.aichat.models.ChatMessage;
import com.mindary.aichat.models.Conversation;
import com.mindary.aichat.models.ConversationStatus;
import com.mindary.aichat.models.FollowUpAnalysis;
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
        // Generate AI response to initial message
        String response = geminiService.generateResponse(initialMessage, "", null);

        // create and save conversation
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(generateTitle(initialMessage));
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessage(initialMessage);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation = conversationRepository.save(conversation);

        // create and save initial chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversation.getId());
        chatMessage.setUserId(userId);
        chatMessage.setType(MessageType.USER);
        chatMessage.setMessage(initialMessage);
        chatMessage.setResponse(response);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);

        // analyze for potential follow-up
        analyzeAndScheduleFollowUp(conversation.getId(), initialMessage);

        return conversation;
    }

    private void analyzeAndScheduleFollowUp(String conversationId, String message) {
        FollowUpAnalysis analysis = geminiService.analyzeForFollowUp(message);
        if (analysis.isNeedsFollowUp()) {
            Conversation conversation = getConversation(conversationId);
            scheduleFollowUp(
                    conversation,
                    analysis.getFollowUpType(),
                    LocalDateTime.now().plusHours(analysis.getFollowUpHours())
            );
        }
    }

    public List<ChatMessage> getConversationHistory(String conversationId) {
        return chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    private String generateTitle(String message) {
        // generate title with my man Gemini
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
        try {
            log.info("Fetching conversation: {}", conversationId);
            return conversationRepository.findById(conversationId)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching conversation {}: {}", conversationId, e.getMessage());
            return null;
        }
    }

    public String deleteConversation(String conversationId) {
        try {
            log.info("Attempting to delete conversation: {}", conversationId);
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                log.warn("Conversation not found: {}", conversationId);
                return null;
            }

            String title = conversation.getTitle();
            chatMessageRepository.deleteByConversationId(conversationId);
            conversationRepository.deleteById(conversationId);
            log.info("Successfully deleted conversation: {} ({})", title, conversationId);
            return title;
        } catch (Exception e) {
            log.error("Failed to delete conversation {}: {}", conversationId, e.getMessage());
            return null;
        }
    }

    public void deleteMessage(String conversationId, String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Verify message belongs to conversation
        if (!message.getConversationId().equals(conversationId)) {
            throw new RuntimeException("Message does not belong to this conversation");
        }

        chatMessageRepository.deleteById(messageId);
    }

    public List<Map<String, Object>> getUserConversationSummaries(UUID userId) {
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);

        return conversations.stream().map(conv -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", conv.getId());
            summary.put("title", conv.getTitle());
            summary.put("lastMessage", conv.getLastMessage());
            summary.put("lastMessageAt", conv.getLastMessageAt());
            summary.put("status", conv.getStatus());

            // Get message count
            long messageCount = chatMessageRepository.countByConversationId(conv.getId());
            summary.put("messageCount", messageCount);

            return summary;
        }).toList();
    }
}
