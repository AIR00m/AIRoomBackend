package com.airoom.airoom.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 들어오기 전에 여러가지 보안 필터를 거치게 되는데 그것을 여기서 설정
        // -> JWT 필터 추가, CORS/CSRF, 커스텀 필터 설정

        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .formLogin(form -> form.disable()); // 로그인 폼 비활성화
        return http.build();
    }

    // 패스워드를 암호화하는 인코더
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 로그인 시 아이디/비밀번호 검증을 하는 매니저
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    // 예외를 처리하는 핸들러



}
