package com.mindary.aichat.controllers;

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
        // Generate AI response first
        String aiResponse = geminiService.generateResponse(chatRequest.getMessage(), "", null);

        Conversation conversation = conversationService.createConversation(
                chatRequest.getUserId(),
                chatRequest.getMessage(),
                aiResponse
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

        String response = geminiService.generateResponse(
                chatRequest.getMessage(),
                conversationId, // Pass conversationId instead of history string
                null // diary insights will be added later
        );

        // Let ConversationService handle the message saving and analysis
        return ResponseEntity.ok(conversationService.saveMessage(
                conversationId,
                chatRequest.getUserId(),
                chatRequest.getMessage(),
                response
        ));
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
