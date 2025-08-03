package com.airoom.airoom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AiRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiRoomApplication.class, args);
    }

}
