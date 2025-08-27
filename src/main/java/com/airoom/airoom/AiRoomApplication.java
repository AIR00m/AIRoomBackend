package com.airoom.airoom;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class AiRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiRoomApplication.class, args);
    }

    @PostConstruct
    public void started() {
        // JVM의 시간대를 Asia/Seoul로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
