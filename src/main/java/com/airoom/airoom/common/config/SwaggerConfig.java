package com.airoom.airoom.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                .pathsToMatch("/**")
                .packagesToScan("com.airoom.airoom")
                .build();
    }

    @Bean
    public OpenAPI jwtOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // 인증 방식이 HTTP(S) 헤더임을 명시
                .scheme("bearer")               // Authorization: Bearer <token> 형태
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)   // 헤더 이름
                .name("Authorization");         // 사용할 헤더 이름

        // 컴포넌트에 담아서 나중에 참조해서 사용이 가능하다.
        Components components = new Components()
                .addSecuritySchemes("bearer", securityScheme);

        // 전역 보안 요구사항 : 모든 API에 bearer 스키마를 적용 -> JWT를 적용한다.
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearer");

        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement);

    }
}
