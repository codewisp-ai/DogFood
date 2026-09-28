package com.dogfood.observability.repository;

import com.dogfood.observability.entity.ServiceHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ServiceHealthRepository extends JpaRepository<ServiceHealth, UUID> {
    Optional<ServiceHealth> findByServiceName(String serviceName);
}
