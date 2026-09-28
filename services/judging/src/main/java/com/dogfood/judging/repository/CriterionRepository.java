package com.dogfood.judging.repository;

import com.dogfood.judging.entity.Criterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CriterionRepository extends JpaRepository<Criterion, UUID> {
    List<Criterion> findByRubricId(UUID rubricId);
    List<Criterion> findByRubricIdOrderBySortOrderAsc(UUID rubricId);
}
