package com.dogfood.event.bulk;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface ExportJobRepository extends JpaRepository<ExportJob, UUID> {
    List<ExportJob> findByEventIdOrderByCreatedAtDesc(UUID eventId);
}
