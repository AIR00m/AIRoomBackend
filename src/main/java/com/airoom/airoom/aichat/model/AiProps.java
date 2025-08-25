package com.airoom.airoom.aichat.model;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@ConfigurationProperties(prefix = "ai")
public class AiProps {
    private OpenAi openai = new OpenAi();
    private Qdrant qdrant = new Qdrant();

    @Getter
    public static class OpenAi {
        private String apiKey;
        private String baseUrl;
        private String chatModel;
        private String embeddingModel;
    }
    @Getter
    public static class Qdrant {
        private String url;
        private String collection;
        private int topK;
    }
}
