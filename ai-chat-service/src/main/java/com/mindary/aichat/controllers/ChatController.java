package com.mindary.aichat.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindary.aichat.dto.ChatRequest;
import com.mindary.aichat.models.ChatMessage;
import com.mindary.aichat.models.Conversation;
import com.mindary.aichat.models.FollowUpAnalysis;
import com.mindary.aichat.models.MessageType;
import com.mindary.aichat.repositories.ChatMessageRepository;
import com.mindary.aichat.services.ConversationService;
import com.mindary.aichat.services.GeminiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = "/api/v1/chat", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final GeminiService geminiService;
    private final ConversationService conversationService;
    private final ChatMessageRepository chatMessageRepository;
    private static final int CHAT_HISTORY_LIMIT = 7; // Limit to last 7 messages for development v1

    @PostMapping("/conversations")
    public ResponseEntity<Map<String, Object>> createConversation(@Valid @RequestBody ChatRequest chatRequest) {
        Conversation conversation = conversationService.createConversation(
                chatRequest.getUserId(),
                chatRequest.getMessage()
        );

        List<ChatMessage> messages = conversationService.getConversationHistory(conversation.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("conversation", conversation);
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/user/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable UUID userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @PostMapping(value = "/conversations/{conversationId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody ChatRequest chatRequest) {

        // Get conversation history
        List<ChatMessage> chatHistory = chatMessageRepository
                .findByConversationIdOrderByTimestampDesc(conversationId)
                .stream()
                .limit(CHAT_HISTORY_LIMIT)
                .toList();

        StringBuilder historyBuilder = new StringBuilder();
        if (!chatHistory.isEmpty()) {
            for (ChatMessage msg : chatHistory) {
                historyBuilder.append(msg.getType()).append(": ")
                        .append(msg.getMessage()).append("\n");
            }
        }

        String response = geminiService.generateResponse(
                chatRequest.getMessage(),
                historyBuilder.toString(),
                null // diary insights will be added later
        );

        // Analyze message for follow-up scheduling
        analyzeAndScheduleFollowUp(conversationId, chatRequest.getMessage());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setConversationId(conversationId);
        chatMessage.setUserId(chatRequest.getUserId());
        chatMessage.setMessage(chatRequest.getMessage());
        chatMessage.setResponse(response);
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessage.setType(MessageType.USER);

        return ResponseEntity.ok(chatMessageRepository.save(chatMessage));
    }

    private void analyzeAndScheduleFollowUp(String conversationId, String message) {
        // Use Gemini to analyze if the message indicates a need for follow-up
        FollowUpAnalysis analysis = geminiService.analyzeForFollowUp(message);
        if (analysis.needsFollowUp()) {
            Conversation conversation = conversationService.getConversation(conversationId);
            conversationService.scheduleFollowUp(
                    conversation,
                    analysis.getFollowUpType(),
                    LocalDateTime.now().plusHours(analysis.getFollowUpHours())
            );
        }
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable UUID userId) {
        List<ChatMessage> chatHistory = chatMessageRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(chatHistory);
    }

    @DeleteMapping("/history/{userId}")
    public ResponseEntity<Void> deleteChatHistory(@PathVariable UUID userId) {
        chatMessageRepository.deleteByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable String conversationId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        List<ChatMessage> messages = conversationService.getConversationHistory(conversationId);
        Map<String, Object> response = new HashMap<>();
        response.put("conversation", conversation);
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Map<String, String>> deleteConversation(@PathVariable String conversationId) {
        String title = conversationService.deleteConversation(conversationId);
        Map<String, String> response = new HashMap<>();

        if (title == null) {
            response.put("error", "Cannot delete conversation: not found or error occurred");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", String.format("Successfully deleted conversation: %s", title));
        return ResponseEntity.ok(response);
    }

    // Get all users conversations with summary
    @GetMapping("/users/{userId}/conversations")
    public ResponseEntity<List<Map<String, Object>>> getUserConversationSummaries(@PathVariable UUID userId) {
        return ResponseEntity.ok(conversationService.getUserConversationSummaries(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<ChatMessage>> getConversationMessages(@PathVariable String conversationId) {
        return ResponseEntity.ok(conversationService.getConversationHistory(conversationId));
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String conversationId,
            @PathVariable String messageId) {
        conversationService.deleteMessage(conversationId, messageId);
        return ResponseEntity.ok().build();
    }
}
