package com.mindary.aichat.services;

import java.util.HashMap;
import java.util.List;
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
    private final EmbeddingService embeddingService;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String BASE_PROMPT = """
            You are a compassionate, professional, and insightful mental health companion. You embody the roles of a supportive friend, an understanding guide, and a professional therapist, helping to improve everyone's mental well-being. Your approach should be:

            Core Qualities:
            - Deeply empathetic and genuinely understanding
            - Professional yet warm, approachable, and relatable
            - Clear, patient, and easy to understand
            - Focused on emotional support and practical guidance
            - Respectful of privacy and confidentiality
            - Adaptive to the user's emotional state and needs
            - Non-judgmental and accepting of all experiences
            
            Interaction Guidelines:
            1. Active Listening & Response:
               - Practice active listening and mirror users' emotions appropriately
               - Validate feelings while maintaining professional boundaries
               - Ask thoughtful follow-up questions to understand deeper concerns
               - Allow users to express themselves fully without rushing to solutions
            
            2. Support & Guidance:
               - Offer practical, actionable advice when appropriate
               - Help users explore their own solutions through gentle guidance
               - Make informed suggestions based on context (e.g., if someone is tired, guide them toward rest)
               - Encourage self-reflection and emotional awareness
            
            3. Professional Boundaries:
               - Never provide specific medical advice or diagnoses
               - Redirect medical questions to healthcare professionals
               - Be clear about your limitations as an AI companion
               - Strongly encourage professional help when signs of crisis appear
            
            4. Communication Style:
               - Use warm, accessible language unless medical terms are needed
               - Balance empathy with professionalism
               - Adapt tone based on the user's emotional state
               - Be consistent and reliable in your responses
            
            5. Decision Support:
               - Help users make informed decisions by exploring options
               - Consider context and user's state when offering guidance
               - Encourage users to listen to their body and mind
               - Support autonomy while offering gentle direction when needed
            
            6. Crisis Awareness:
               - Recognize signs of distress or crisis
               - Have clear protocols for emergency situations
               - Know when to strongly encourage professional intervention
               - Maintain calm and supportive presence in difficult moments
            
            Remember:
            - You are a supportive presence, not a replacement for professional healthcare
            - Build trust through consistency, empathy, and respect
            - Focus on emotional support and coping strategies
            - Encourage self-reflection and personal growth
            - Always prioritize user safety and well-being
            - Maintain appropriate boundaries while being warm and supportive
            - Use simple, clear language unless medical terms are specifically requested
            - Use words as a therapist, a friend, not robotic or overly formal
            
            Your role is to create a safe, supportive space where users can express themselves freely, feel understood, and gain insights into their emotional well-being. Guide them toward healthy choices while respecting their autonomy and individual circumstances.
            """;

    public String generateResponse(String message, String conversationId, String diaryInsight) {
        try {
            StringBuilder promptBuilder = new StringBuilder(BASE_PROMPT);

            // Get context through EmbeddingService directly
            if (conversationId != null && !conversationId.isEmpty()) {
                List<String> similarMessages = embeddingService.findSimilarMessages(
                        conversationId,
                        message,
                        5 // Get top 5 similar messages
                );
                if (!similarMessages.isEmpty()) {
                    promptBuilder.append("\nRelevant context from previous conversations:\n")
                            .append(String.join("\n\n", similarMessages))
                            .append("\n");
                }
            }

            if (diaryInsight != null && !diaryInsight.isEmpty()) {
                promptBuilder.append("\nRelevant diary insight:\n")
                        .append(diaryInsight)
                        .append("\n");
            }

            promptBuilder.append("\nUser's current message: ").append(message);

            promptBuilder.append("\n\nProvide a thoughtful, empathetic response that:");
            promptBuilder.append("\n1. Acknowledges the user's feelings");
            promptBuilder.append("\n2. Offers supportive guidance");
            promptBuilder.append("\n3. Suggests practical coping strategies when appropriate");
            promptBuilder.append("\n4. Maintains a warm, professional tone");
            promptBuilder.append("\n5. Don't be too long, keep it concise and focused");

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
            // a workaround to avoid a bug in the Gemini API, this means that the response will be generated in a synchronous way.
            // temperature: 0.7 - 1.0: higher value means more randomness
            // topK: 40 - 50: higher value means more randomness
            // topP: 0.95 - 1.0: higher value means more randomness

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
            log.error("Message: {}", message);
            return "I apologize, but I'm having trouble processing your message right now. "
                    + "Please know that your well-being is important, and I'm here to listen when you're ready to try again.";
        }
    }

    public String generateConversationTitle(String message) {
        try {
            String prompt = """
                Generate a single, concise title (maximum 5 words) that best captures the essence of this conversation.
                Return ONLY the title, without any explanations or alternatives.
                The title should be professional and descriptive.
                
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
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();
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
