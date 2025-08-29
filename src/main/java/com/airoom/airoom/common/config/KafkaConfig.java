package com.airoom.airoom.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userActivityTopic() {
        return new NewTopic("user-activity", 3, (short) 1);
    }

    @Bean
    public NewTopic examEventTopic() {
        return new NewTopic("exam-logs", 3, (short) 1);
    }

    @Bean
    public NewTopic examResultTopic() {
        return new NewTopic("class-logs", 3, (short) 1);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}