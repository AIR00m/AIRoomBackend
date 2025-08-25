package com.airoom.airoom.aichat.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Getter @Setter
public class AiProps {
    private OpenAi openai = new OpenAi();
    private Qdrant qdrant = new Qdrant();

    @Getter @Setter
    public static class OpenAi {
        private String apiKey;
        private String baseUrl;
        private String chatModel;
        private String embeddingModel;
    }
    @Getter @Setter
    public static class Qdrant {
        private String url;
        private String collection;
        private int topK;
    }
}
