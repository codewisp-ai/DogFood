package com.dogfood.judging.security;

import com.dogfood.common.security.RequestContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AOP aspect that automatically sets RLS session variables for methods that access scores.
 * This provides defense-in-depth security - even if application-layer filtering has bugs,
 * the database-level RLS policies will enforce judge isolation.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RlsAspect {

    private final RlsSessionManager rlsSessionManager;

    /**
     * Automatically set RLS variables before any repository method that could access scores.
     * This ensures RLS policies are active whenever scores are queried.
     */
    @Before("execution(* com.dogfood.judging.repository.ScoreRepository.*(..)) || " +
            "execution(* com.dogfood.judging.repository.NormalizedScoreRepository.*(..))")
    public void setRlsVariables(JoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = RequestContext.getUserId(request) != null ? RequestContext.getUserId(request).toString() : null;
                String roles = String.join(",", RequestContext.getUserRoles(request));
                
                if (userId != null) {
                    rlsSessionManager.setSessionVariables(userId, roles);
                    log.debug("RLS variables set for method {} with userId={}, roles={}", 
                             joinPoint.getSignature().getName(), userId, roles);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to set RLS variables for {}: {}", 
                    joinPoint.getSignature().getName(), e.getMessage());
            // Don't fail the operation - RLS is defense-in-depth, not primary security
        }
    }
}