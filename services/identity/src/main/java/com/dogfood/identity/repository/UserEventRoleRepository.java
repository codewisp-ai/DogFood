package com.dogfood.identity.repository;

import com.dogfood.identity.entity.UserEventRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEventRoleRepository extends JpaRepository<UserEventRole, UUID> {
    Optional<UserEventRole> findByUserIdAndEventId(UUID userId, UUID eventId);
    List<UserEventRole> findByUserId(UUID userId);
    List<UserEventRole> findByEventIdAndRole(UUID eventId, String role);
}
