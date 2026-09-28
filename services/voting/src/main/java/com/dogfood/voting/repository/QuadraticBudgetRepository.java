package com.dogfood.voting.repository;

import com.dogfood.voting.entity.QuadraticBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QuadraticBudgetRepository extends JpaRepository<QuadraticBudget, UUID> {
    Optional<QuadraticBudget> findByEventIdAndVoterId(UUID eventId, UUID voterId);
}
