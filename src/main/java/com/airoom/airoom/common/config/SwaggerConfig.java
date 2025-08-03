package com.airoom.airoom.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AIRoom 아이룸 API",
                description = "API 명세서",
                version = "V1"
        )
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("api-definition")
                .pathsToMatch("/api/**")
                .packagesToScan("com.airoom.airoom")
                .build();
    }
}
