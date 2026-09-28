package com.dogfood.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway security configuration.
 * Performs AUTHENTICATION only — never authorization.
 * Authorization is enforced by each backend service using JWT claims injected as headers.
 *
 * Public paths (no JWT required): auth endpoints, gallery, status, JWKS, actuator health.
 * All other paths require a valid JWT.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints — no JWT required
                        .pathMatchers("/api/auth/register", "/api/auth/login",
                                "/api/auth/refresh", "/api/auth/.well-known/**").permitAll()
                        .pathMatchers("/api/events", "/api/events/**").permitAll()
                        .pathMatchers("/api/submissions/**").permitAll()
                        .pathMatchers("/api/gallery/**").permitAll()
                        .pathMatchers("/status", "/api/status").permitAll()
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        // Everything else requires authentication
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
