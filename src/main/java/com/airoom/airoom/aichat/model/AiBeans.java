package com.airoom.airoom.aichat.model;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(AiProps.class)
@RequiredArgsConstructor
public class AiBeans {

    private final AiProps props;

    @Bean("openaiWebClient")
    public WebClient openaiWebClient() {
        return WebClient.builder()
                .baseUrl(props.getOpenai().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getOpenai().getApiKey())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .build();
    }

    @Bean("qdrantWebClient")
    public WebClient qdrantWebClient() {
        return WebClient.builder()
                .baseUrl(props.getQdrant().getUrl())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .build();
    }
}
