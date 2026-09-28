package com.dogfood.observability;

import com.dogfood.observability.entity.ServiceHealth;
import com.dogfood.observability.repository.ServiceHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthPoller {
    private final ServiceHealthRepository healthRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedDelay = 60000)
    public void pollHealth() {
        String serviceName = "gateway";
        String status = "DOWN";
        String details = "Failed to fetch health";
        try {
            String url = "http://gateway:8080/actuator/health";
            String response = restTemplate.getForObject(url, String.class);
            if (response != null && response.contains("\"status\":\"UP\"")) {
                status = "UP";
            }
            details = response;
        } catch (Exception e) {
            log.error("Error polling gateway health", e);
            details = e.getMessage();
        }

        ServiceHealth health = healthRepository.findByServiceName(serviceName)
                .orElse(ServiceHealth.builder().id(UUID.randomUUID()).serviceName(serviceName).build());
        health.setStatus(status);
        health.setLastChecked(Instant.now());
        health.setDetails(details);
        healthRepository.save(health);
    }
}
