package com.bahubba.bahubbabookclub.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("*"));
                config.addAllowedHeader("Accept");
                config.addAllowedHeader("Content-Type");
                config.addAllowedHeader("X-Requested-With");
                config.addAllowedHeader("Authorization");
                config.addAllowedHeader("Access-Control-Allow-Origin");
                config.setAllowCredentials(true);
                config.addAllowedMethod("GET");
                config.addAllowedMethod("PUT");
                config.addAllowedMethod("PATCH");
                config.addAllowedMethod("POST");
                config.addAllowedMethod("DELETE");
                config.addAllowedMethod("OPTIONS");
                config.setMaxAge(3600L);
                return config;
            }))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/api/v1/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated())
            .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
            .build();
    }
}
