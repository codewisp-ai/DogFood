package com.dogfood.judging.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Service to set Row Level Security session variables within the current transaction.
 * Called at the beginning of each transactional method that needs RLS enforcement.
 */
@Component
public class RlsSessionManager {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Sets the PostgreSQL session variables used by RLS policies.
     * Must be called within an active transaction.
     */
    @Transactional
    public void setSessionVariables(String userId, String role) {
        if (userId != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_judge_id', :userId, false)")
                         .setParameter("userId", userId)
                         .getSingleResult();
        }
        
        if (role != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_role', :role, false)")
                         .setParameter("role", role)
                         .getSingleResult();
        }
    }
}
