package com.dogfood.event.repository;
import com.dogfood.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
