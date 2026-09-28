package com.dogfood.event.repository;
import com.dogfood.event.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByEventId(UUID eventId);
    Optional<Team> findByEventIdAndName(UUID eventId, String name);
}
