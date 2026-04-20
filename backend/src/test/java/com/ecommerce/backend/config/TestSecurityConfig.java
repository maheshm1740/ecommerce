package com.ecommerce.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Overrides the production SecurityConfig for all @WebMvcTest slices.
 *
 * What this does:
 *  - Disables CSRF so POST/PUT/DELETE tests don't need .with(csrf())
 *  - Keeps stateless sessions (same as prod)
 *  - Permits /api/auth/** without authentication (same as prod)
 *  - Requires authentication for /api/cart/** and /api/orders/**
 *    so @WithMockUser / unauthenticated tests behave correctly
 *  - Does NOT load JwtFilter, JwtUtil, or UserDetailsServiceImpl —
 *    that's the entire point: no transitive bean dependencies to mock
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\": \"Access Denied\"}");
                        })
                )
                .build();
    }
}