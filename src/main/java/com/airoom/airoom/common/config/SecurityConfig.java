package com.airoom.airoom.common.config;

import com.airoom.airoom.common.token.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Primary
    public CorsConfigurationSource CorsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(
                List.of(
                        "http://43.200.2.244", // 배포 프론트 서버
                        "http://43.200.2.244:80",
                        "http://localhost:8080",
                        "http://localhost:5173", // 로컬 Vue 서버
                        "http://localhost:4455", // SecureAgent 4455~4460
                        "http://localhost:4456",
                        "http://localhost:4457",
                        "http://localhost:4458",
                        "http://localhost:4459",
                        "http://localhost:4460",
                        "http://127.0.0.1:4455",
                        "http://127.0.0.1:4456",
                        "http://127.0.0.1:4457",
                        "http://127.0.0.1:4458",
                        "http://127.0.0.1:4459",
                        "http://127.0.0.1:4460",
                        "http://ec2-43-200-2-244.ap-northeast-2.compute.amazonaws.com:443",
                        "http://ec2-43-200-2-244.ap-northeast-2.compute.amazonaws.com:80",
                        "http://ec2-43-200-2-244.ap-northeast-2.compute.amazonaws.com:5173"
                )
        );
        corsConfiguration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS","HEAD"));
        // 허용할 Http 메소드를 입력해준다.
//        corsConfiguration.setAllowedHeaders(List.of("Authorization","Content-Type"));
        // 브라우저 요청 해더에 보내도 되는 것 Authorization -> JWT / JSON -> Content-Type
        corsConfiguration.setAllowedHeaders(List.of("*"));

//        corsConfiguration.setExposedHeaders(List.of("Authorization")); -> Refresh Token에서 필요없음
        // 응답 해더 중 JS 코드 볼 수 있게 해주는 것

        corsConfiguration.setAllowCredentials(true);
        // refresh 토큰을 같이 사용하는 경우 HttpOnly-> 쿠키이므로 허용해야지 사용이 가능함
        // 모두 헤더/ 바디로 넣어서 할 수 있는 방식도 존재 한다.
        corsConfiguration.setMaxAge(3600L);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        // 들어오기 전에 여러가지 보안 필터를 거치게 되는데 그것을 여기서 설정
        // -> JWT 필터 추가, CORS/CSRF, 커스텀 필터 설정

        http
//              .csrf(csrf -> csrf.disable())// CSRF 비활성화
                .cors(c -> c.configurationSource(corsConfigurationSource))
                // 어떤 브라우저에서 우리
                .csrf(AbstractHttpConfigurer::disable) // csrf : jwt 인증이면 비활성화 시킴

                .sessionManagement(sm-> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // 세션 관리 - Stateless -> 세션 저장을 피함
                // Stateless 를 IF_REQUIRED 로 변경해서, secure agent 관련해서 세션 발급되게함

                .exceptionHandling(ex ->ex
                        .authenticationEntryPoint((request,response,exception)->{
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);              // 401: 인증 안 됨
                            response.setContentType("application/json;charset=UTF-8");            // 응답은 JSON
                            response.getWriter().write("{\"message\":\"인증되지 않은 회원입니다.\"}");

                }).accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"권한에 맞지 않은 회원입니다.\"}");
                        })
                )

                .authorizeHttpRequests(auth-> auth
                        .requestMatchers("/api/agent/**").permitAll()
                        .requestMatchers("/auth/**").permitAll() // 인증
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll() // 모니터링
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()// Swagger API
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        // 프리플라이트(브라우저가 실제 요청 전에 서버에 보내는 사전 검사 요청) -> 막히게 되면 실제 API 호출 전에 실패 함
                        .requestMatchers("/install/**","/agent-required/**","/download/agent").permitAll()
                        .anyRequest().authenticated() // 다른 것에 대한것은 인증이 필요
                )

                //  UsernamePasswordAuthenticationFilter 전에 JWT 필터 체인 삽입
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                //  기본 로그인/로그아웃 비활성 -> Rest + JWT 방식으로 진행
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

//               .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()) // 모든 요청 허용
//                .formLogin(form -> form.disable()); // 로그인 폼 비활성화
        return http.build();
    }


    // 패스워드를 암호화하는 인코더
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
