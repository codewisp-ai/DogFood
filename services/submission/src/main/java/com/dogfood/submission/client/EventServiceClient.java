package com.dogfood.submission.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class EventServiceClient {

    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    @Value("${app.event-service.url}")
    private String eventServiceUrl;

    public EventServiceClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = new RestTemplate();
    }

    public Instant getEventDeadline(UUID eventId) {
        String cacheKey = "event:deadline:" + eventId;
        String cachedDeadline = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedDeadline != null) {
            return Instant.parse(cachedDeadline);
        }

        try {
            Map response = restTemplate.getForObject(eventServiceUrl + "/api/events/" + eventId, Map.class);
            if (response != null && response.get("submissionDeadline") != null) {
                Instant deadline = Instant.parse((String) response.get("submissionDeadline"));
                redisTemplate.opsForValue().set(cacheKey, deadline.toString(), 60, TimeUnit.SECONDS);
                return deadline;
            }
        } catch (Exception e) {
            log.error("Failed to fetch event deadline for {}", eventId, e);
        }
        
        // Fallback or handle missing deadline
        return Instant.MAX;
    }
}
