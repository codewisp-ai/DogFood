package com.dogfood.observability.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_health")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealth {
    @Id
    private UUID id;
    private String serviceName;
    private String status;
    private Instant lastChecked;
    // private String details;
}
