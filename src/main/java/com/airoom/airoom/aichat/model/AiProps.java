package com.airoom.airoom.aichat.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai")
public class AiProps {

    private OpenAi openai = new OpenAi();
    private Qdrant qdrant = new Qdrant();

    @Data
    public static class OpenAi {
        @NotBlank
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String chatModel = "gpt-4o-mini";
        private String embeddingModel = "text-embedding-3-small";
    }

    @Data
    public static class Qdrant {
        @NotBlank
        private String url;              // http://qdrant:6333 (운영), http://localhost:6333 (로컬)
        private String collection = "ai_career_ko_v1";
        private int topK = 3;
    }
}

