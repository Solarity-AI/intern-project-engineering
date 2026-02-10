package com.solarityai.productreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the dev profile.
 * Permits all API endpoints since this app uses header-based
 * user identification (X-User-ID), not JWT / session auth.
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll()
            )
            // Required for H2 console iframe to render
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
