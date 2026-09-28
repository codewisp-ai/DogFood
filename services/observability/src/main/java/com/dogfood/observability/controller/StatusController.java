package com.dogfood.observability.controller;

import com.dogfood.observability.entity.ServiceHealth;
import com.dogfood.observability.repository.ServiceHealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StatusController {
    private final ServiceHealthRepository healthRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        List<ServiceHealth> healths = healthRepository.findAll();
        boolean allUp = healths.stream().allMatch(h -> "UP".equals(h.getStatus()));
        
        List<Map<String, Object>> services = healths.stream().map(h -> Map.<String, Object>of(
                "service", h.getServiceName(),
                "status", h.getStatus(),
                "lastChecked", h.getLastChecked()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "status", allUp && !healths.isEmpty() ? "OPERATIONAL" : "DEGRADED",
                "message", allUp && !healths.isEmpty() ? "Dogfood Platform is running smoothly." : "Some services are experiencing issues.",
                "services", services
        ));
    }
}
