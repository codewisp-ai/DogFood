package com.dogfood.judging.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class RlsContextFilter implements Filter {

    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader("X-User-Id");
        String roles = req.getHeader("X-User-Roles");

        if (userId != null) {
            currentUserId.set(userId);
        }
        if (roles != null) {
            String[] roleArray = roles.split(",");
            String primaryRole = java.util.Arrays.stream(roleArray)
                    .filter(role -> role.equals("ADMIN") || role.equals("ORGANIZER"))
                    .findFirst()
                    .orElse(roleArray.length > 0 ? roleArray[0] : "USER");
            currentRole.set(primaryRole);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            currentUserId.remove();
            currentRole.remove();
        }
    }

    public static String getCurrentUserId() {
        return currentUserId.get();
    }

    public static String getCurrentRole() {
        return currentRole.get();
    }
}
