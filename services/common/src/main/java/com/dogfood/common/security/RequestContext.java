package com.dogfood.common.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Extracts user identity from gateway-injected headers.
 * The API Gateway validates the JWT and forwards these headers:
 *   X-User-Id      — UUID of the authenticated user
 *   X-User-Roles   — comma-separated roles (PARTICIPANT, JUDGE, ORGANIZER, ADMIN)
 *   X-User-Email   — user's email
 *   X-Correlation-ID — request correlation ID for distributed tracing
 *
 * Backend services NEVER parse JWTs directly — they trust these headers
 * because the gateway is the sole network entry point.
 */
public final class RequestContext {
    private RequestContext() {}

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_IDEMPOTENCY_KEY = "Idempotency-Key";

    public static UUID getUserId(HttpServletRequest request) {
        String header = request.getHeader(HEADER_USER_ID);
        if (header == null || header.isBlank()) return null;
        return UUID.fromString(header);
    }

    public static List<String> getUserRoles(HttpServletRequest request) {
        String header = request.getHeader(HEADER_USER_ROLES);
        if (header == null || header.isBlank()) return Collections.emptyList();
        return Arrays.asList(header.split(","));
    }

    public static String getUserEmail(HttpServletRequest request) {
        return request.getHeader(HEADER_USER_EMAIL);
    }

    public static String getCorrelationId(HttpServletRequest request) {
        return request.getHeader(HEADER_CORRELATION_ID);
    }

    public static String getIdempotencyKey(HttpServletRequest request) {
        return request.getHeader(HEADER_IDEMPOTENCY_KEY);
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return getUserRoles(request).stream()
                .anyMatch(r -> r.equals(role) || r.endsWith(":" + role));
    }

    public static boolean isOrganizer(HttpServletRequest request) {
        return hasRole(request, "ORGANIZER") || hasRole(request, "ADMIN");
    }

    public static boolean isJudge(HttpServletRequest request) {
        return hasRole(request, "JUDGE");
    }

    public static boolean isAdmin(HttpServletRequest request) {
        return hasRole(request, "ADMIN");
    }

    public static String getCorrelationId() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs = 
                (org.springframework.web.context.request.ServletRequestAttributes) 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                String header = attrs.getRequest().getHeader(HEADER_CORRELATION_ID);
                if (header != null && !header.isBlank()) {
                    return header;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return UUID.randomUUID().toString();
    }
}
