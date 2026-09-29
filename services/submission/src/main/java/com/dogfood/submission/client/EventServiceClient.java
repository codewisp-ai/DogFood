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

    public Instant getEventStartDate(UUID eventId) {
        String cacheKey = "event:startdate:" + eventId;
        String cachedDate = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedDate != null) {
            return Instant.parse(cachedDate);
        }

        try {
            Map response = restTemplate.getForObject(eventServiceUrl + "/api/events/" + eventId, Map.class);
            if (response != null && response.get("startDate") != null) {
                Instant startDate = Instant.parse((String) response.get("startDate"));
                redisTemplate.opsForValue().set(cacheKey, startDate.toString(), 60, TimeUnit.SECONDS);
                return startDate;
            } else if (response != null && response.get("createdAt") != null) {
                // Fallback to createdAt if startDate is missing
                Instant startDate = Instant.parse((String) response.get("createdAt"));
                redisTemplate.opsForValue().set(cacheKey, startDate.toString(), 60, TimeUnit.SECONDS);
                return startDate;
            }
        } catch (Exception e) {
            log.error("Failed to fetch event start date for {}", eventId, e);
        }
        
        return Instant.EPOCH;
    }

    public boolean isTeamMember(UUID teamId, UUID userId) {
        if (userId == null || teamId == null) return false;
        try {
            java.util.List members = restTemplate.getForObject(eventServiceUrl + "/api/teams/" + teamId + "/members", java.util.List.class);
            if (members != null) {
                for (Object m : members) {
                    if (m instanceof Map map && userId.toString().equals(map.get("userId"))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check team membership: {}", e.getMessage());
        }
        return false;
    }

    public java.util.List<UUID> getUserTeamIds(UUID eventId, UUID userId) {
        if (userId == null || eventId == null) return java.util.List.of();
        try {
            java.util.List teams = restTemplate.getForObject(eventServiceUrl + "/api/events/" + eventId + "/teams", java.util.List.class);
            if (teams == null) return java.util.List.of();
            java.util.List<UUID> myTeams = new java.util.ArrayList<>();
            for (Object t : teams) {
                if (t instanceof Map teamMap) {
                    UUID teamId = UUID.fromString((String) teamMap.get("id"));
                    if (userId.toString().equals(teamMap.get("createdBy")) || isTeamMember(teamId, userId)) {
                        myTeams.add(teamId);
                    }
                }
            }
            return myTeams;
        } catch (Exception e) {
            log.warn("Failed to get user teams: {}", e.getMessage());
            return java.util.List.of();
        }
    }
}
