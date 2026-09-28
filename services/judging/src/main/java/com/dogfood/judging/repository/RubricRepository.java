package com.dogfood.judging.repository;

import com.dogfood.judging.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, UUID> {
    Optional<Rubric> findByEventId(UUID eventId);
}
