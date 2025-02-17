package com.mindary.aichat.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindary.aichat.config.GeminiConfig;
import com.mindary.aichat.models.FollowUpAnalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    private static final String BASE_PROMPT = """
            You are a compassionate and professional mental health counselor. Your responses should be:
            - Empathetic and understanding
            - Professional yet warm and approachable
            - Clear and easy to understand
            - Focused on emotional support and practical guidance
            - Respectful of privacy and confidentiality
            
            Guidelines:
            1. Use simple, clear language unless medical terms are specifically requested
            2. Maintain a supportive and non-judgmental tone
            3. Avoid making definitive medical diagnoses
            4. Encourage professional help when appropriate
            5. Prioritize user safety and well-being
            6. Respect boundaries and privacy
            7. Focus on emotional support and coping strategies
            
            Remember: You're here to listen, support, and guide, not to replace professional medical advice.
            """;

    public String generateResponse(String message, String chatHistory, String diaryInsight) {
        try {
            StringBuilder promptBuilder = new StringBuilder(BASE_PROMPT);

            if (chatHistory != null && !chatHistory.isEmpty()) {
                promptBuilder.append("\nPrevious conversation context:\n")
                        .append(chatHistory)
                        .append("\n");
            }

            promptBuilder.append("\nUser's current message: ").append(message);

            promptBuilder.append("\n\nProvide a thoughtful, empathetic response that:");
            promptBuilder.append("\n1. Acknowledges the user's feelings");
            promptBuilder.append("\n2. Offers supportive guidance");
            promptBuilder.append("\n3. Suggests practical coping strategies when appropriate");
            promptBuilder.append("\n4. Maintains a warm, professional tone");

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", promptBuilder.toString())
            });
            requestBody.put("contents", new Object[]{contents});
            requestBody.put("generationConfig", Map.of(
                    "temperature", 0.7,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
            ));

            // Make request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            log.error("Error generating response from Gemini", e);
            return "I apologize, but I'm having trouble processing your message right now. "
                    + "Please know that your well-being is important, and I'm here to listen when you're ready to try again.";
        }
    }

    public String generateConversationTitle(String message) {
        try {
            String prompt = "Generate a short, concise title (max 5 words) for a conversation that starts with this message: " + message;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", prompt)
            });
            requestBody.put("contents", new Object[]{contents});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Error generating conversation title", e);
            return "New Conversation";
        }
    }

    public FollowUpAnalysis analyzeForFollowUp(String message) {
        try {
            String prompt = """
                Analyze this message for potential follow-up needs. Consider:
                1. Health issues that need checking
                2. Anxiety or stress indicators
                3. Sleep-related problems
                4. Mood concerns
                
                Return a JSON with:
                {
                    "needsFollowUp": true/false,
                    "followUpType": "HEALTH_CHECK"/"ANXIETY_CHECK"/"SLEEP_CHECK"/"MOOD_CHECK"/"GENERAL_CHECK",
                    "followUpHours": number (between 8-24)
                }
                
                Message: """ + message;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", geminiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", new Object[]{
                Map.of("text", prompt)
            });
            requestBody.put("contents", new Object[]{contents});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(
                    GEMINI_API_URL + "?key=" + geminiConfig.getApiKey(),
                    request,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String analysisJson = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return mapper.readValue(analysisJson, FollowUpAnalysis.class);
        } catch (Exception e) {
            log.error("Error analyzing for follow-up", e);
            FollowUpAnalysis analysis = new FollowUpAnalysis();
            analysis.setNeedsFollowUp(false);
            return analysis;
        }
    }
}
