package com.dogfood.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Global filter that extracts user identity from the validated JWT
 * and injects it as HTTP headers for downstream services.
 *
 * Downstream services read these headers via RequestContext — they never
 * parse JWTs directly. This keeps authorization logic in backend services
 * while the gateway handles only authentication.
 *
 * Headers injected:
 *   X-User-Id     — UUID from JWT 'sub' claim
 *   X-User-Roles  — comma-separated roles from JWT 'roles' claim
 *   X-User-Email  — email from JWT 'email' claim
 */
@Component
public class UserHeaderEnrichmentFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(jwtAuth -> {
                    Jwt jwt = jwtAuth.getToken();
                    String userId = jwt.getSubject();
                    List<String> roles = jwt.getClaimAsStringList("eventRoles");
                    String email = jwt.getClaimAsString("email");

                    ServerHttpRequest mutatedRequest = new org.springframework.http.server.reactive.ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public org.springframework.http.HttpHeaders getHeaders() {
                            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                            headers.putAll(super.getHeaders());
                            if (userId != null) headers.set("X-User-Id", userId);
                            if (roles != null) headers.set("X-User-Roles", String.join(",", roles));
                            if (email != null) headers.set("X-User-Email", email);
                            return headers;
                        }
                    };

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        // Run after security filter chain
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
