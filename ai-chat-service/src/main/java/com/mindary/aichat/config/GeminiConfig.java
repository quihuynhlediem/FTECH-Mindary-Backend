package com.mindary.aichat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key}") // take from application.properties
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}
