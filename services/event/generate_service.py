import os

base_dir = "/Users/yash/Desktop/dogfood/services/event"

files = {
"build.gradle.kts": """plugins {
    id("org.springframework.boot") version "3.3.0"
    id("io.spring.dependency-management") version "1.1.5"
    java
}

group = "com.dogfood"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":services:common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.3")
    
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
""",

"Dockerfile": """FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
""",

"src/main/resources/application.yml": """server:
  port: 8082

spring:
  application:
    name: event-service
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/dogfood?currentSchema=events
    username: ${DB_USER:postgres}
    password: ${DB_PASS:postgres}
  flyway:
    enabled: true
    schemas: events
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
""",

"src/main/resources/db/migration/V1__create_events_schema.sql": """CREATE SCHEMA IF NOT EXISTS events;

CREATE TABLE IF NOT EXISTS events.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    banner_url VARCHAR(512),
    organizer_id UUID NOT NULL,
    registration_opens_at TIMESTAMPTZ,
    registration_closes_at TIMESTAMPTZ,
    submission_opens_at TIMESTAMPTZ,
    submission_deadline TIMESTAMPTZ NOT NULL,
    judging_opens_at TIMESTAMPTZ,
    judging_closes_at TIMESTAMPTZ,
    voting_opens_at TIMESTAMPTZ,
    voting_closes_at TIMESTAMPTZ,
    results_published_at TIMESTAMPTZ,
    judging_mode VARCHAR(20) DEFAULT 'RUBRIC' CHECK (judging_mode IN ('RUBRIC', 'PAIRWISE')),
    voting_mode VARCHAR(20) DEFAULT 'SIMPLE' CHECK (voting_mode IN ('SIMPLE', 'QUADRATIC')),
    calibration_required BOOLEAN DEFAULT TRUE,
    webhooks_enabled BOOLEAN DEFAULT FALSE,
    voting_access_mode VARCHAR(20) DEFAULT 'AUTHENTICATED' CHECK (voting_access_mode IN ('OPEN', 'EMAIL_GATED', 'AUTHENTICATED')),
    quadratic_vote_budget INTEGER DEFAULT 100,
    eligibility_rules JSONB DEFAULT '[]',
    custom_questions JSONB DEFAULT '[]',
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'OPEN', 'SUBMISSIONS', 'JUDGING', 'VOTING', 'CLOSED')),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.tracks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.prizes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    track_id UUID REFERENCES events.tracks(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events.teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES events.events(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(event_id, name)
);

CREATE TABLE IF NOT EXISTS events.team_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES events.teams(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER' CHECK (role IN ('LEADER', 'MEMBER')),
    joined_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(team_id, user_id)
);

CREATE TABLE IF NOT EXISTS events.team_invites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID REFERENCES events.teams(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255),
    expires_at TIMESTAMPTZ NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_events_slug ON events.events(slug);
CREATE INDEX idx_events_status ON events.events(status);
CREATE INDEX idx_tracks_event ON events.tracks(event_id);
CREATE INDEX idx_teams_event ON events.teams(event_id);
CREATE INDEX idx_team_members_user ON events.team_members(user_id);
CREATE INDEX idx_team_invites_token ON events.team_invites(token);
""",

"src/main/java/com/dogfood/event/Application.java": """package com.dogfood.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
""",

"src/main/java/com/dogfood/event/config/SecurityConfig.java": """package com.dogfood.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
""",

"src/main/java/com/dogfood/event/config/RabbitConfig.java": """package com.dogfood.event.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
""",

"src/main/java/com/dogfood/event/entity/EventStatus.java": """package com.dogfood.event.entity;
public enum EventStatus { DRAFT, OPEN, SUBMISSIONS, JUDGING, VOTING, CLOSED }
""",

"src/main/java/com/dogfood/event/entity/TeamRole.java": """package com.dogfood.event.entity;
public enum TeamRole { LEADER, MEMBER }
""",

"src/main/java/com/dogfood/event/entity/Event.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "events", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String slug;
    private String description;
    
    @Column(name = "banner_url")
    private String bannerUrl;
    
    @Column(name = "organizer_id")
    private UUID organizerId;

    @Column(name = "registration_opens_at")
    private OffsetDateTime registrationOpensAt;

    @Column(name = "registration_closes_at")
    private OffsetDateTime registrationClosesAt;

    @Column(name = "submission_opens_at")
    private OffsetDateTime submissionOpensAt;

    @Column(name = "submission_deadline")
    private OffsetDateTime submissionDeadline;

    @Column(name = "judging_opens_at")
    private OffsetDateTime judgingOpensAt;

    @Column(name = "judging_closes_at")
    private OffsetDateTime judgingClosesAt;

    @Column(name = "voting_opens_at")
    private OffsetDateTime votingOpensAt;

    @Column(name = "voting_closes_at")
    private OffsetDateTime votingClosesAt;

    @Column(name = "results_published_at")
    private OffsetDateTime resultsPublishedAt;

    @Column(name = "judging_mode")
    private String judgingMode;

    @Column(name = "voting_mode")
    private String votingMode;

    @Column(name = "calibration_required")
    private Boolean calibrationRequired;

    @Column(name = "webhooks_enabled")
    private Boolean webhooksEnabled;

    @Column(name = "voting_access_mode")
    private String votingAccessMode;

    @Column(name = "quadratic_vote_budget")
    private Integer quadraticVoteBudget;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "eligibility_rules", columnDefinition = "jsonb")
    private List<Map<String, Object>> eligibilityRules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_questions", columnDefinition = "jsonb")
    private List<Map<String, Object>> customQuestions;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Integer version;
}
""",

"src/main/java/com/dogfood/event/entity/Track.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tracks", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private String name;
    private String description;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
}
""",

"src/main/java/com/dogfood/event/entity/Prize.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "prizes", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prize {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private Track track;

    private String name;
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
""",

"src/main/java/com/dogfood/event/entity/Team.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "teams", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private String name;
    
    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
""",

"src/main/java/com/dogfood/event/entity/TeamMember.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private TeamRole role;

    @CreationTimestamp
    @Column(name = "joined_at")
    private OffsetDateTime joinedAt;
}
""",

"src/main/java/com/dogfood/event/entity/TeamInvite.java": """package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_invites", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String token;
    private String email;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    private Boolean accepted;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
""",

"src/main/java/com/dogfood/event/repository/EventRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
""",

"src/main/java/com/dogfood/event/repository/TrackRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {
    List<Track> findByEventId(UUID eventId);
}
""",

"src/main/java/com/dogfood/event/repository/PrizeRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PrizeRepository extends JpaRepository<Prize, UUID> {
    List<Prize> findByEventId(UUID eventId);
}
""",

"src/main/java/com/dogfood/event/repository/TeamRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByEventId(UUID eventId);
    Optional<Team> findByEventIdAndName(UUID eventId, String name);
}
""",

"src/main/java/com/dogfood/event/repository/TeamMemberRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
    List<TeamMember> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);
    long countByTeamId(UUID teamId);
}
""",

"src/main/java/com/dogfood/event/repository/TeamInviteRepository.java": """package com.dogfood.event.repository;
import com.dogfood.event.entity.TeamInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
    Optional<TeamInvite> findByToken(String token);
}
""",

"src/main/java/com/dogfood/event/dto/EventDtos.java": """package com.dogfood.event.dto;

import com.dogfood.event.entity.EventStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EventDtos {
    public record CreateEventRequest(
        @NotBlank String name,
        String description,
        String bannerUrl,
        @NotNull OffsetDateTime submissionDeadline,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions
    ) {}

    public record UpdateEventRequest(
        String name,
        String description,
        OffsetDateTime submissionDeadline,
        EventStatus status,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions
    ) {}

    public record EventResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String bannerUrl,
        UUID organizerId,
        OffsetDateTime submissionDeadline,
        EventStatus status,
        List<Map<String, Object>> eligibilityRules,
        List<Map<String, Object>> customQuestions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {}

    public record CreateTrackRequest(@NotBlank String name, String description, Integer sortOrder) {}
    public record TrackResponse(UUID id, UUID eventId, String name, String description, Integer sortOrder) {}
}
""",

"src/main/java/com/dogfood/event/dto/TeamDtos.java": """package com.dogfood.event.dto;

import com.dogfood.event.entity.TeamRole;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public class TeamDtos {
    public record CreateTeamRequest(@NotBlank String name) {}
    public record TeamResponse(UUID id, UUID eventId, String name, UUID createdBy, OffsetDateTime createdAt) {}
    public record TeamMemberResponse(UUID id, UUID teamId, UUID userId, TeamRole role, OffsetDateTime joinedAt) {}
    
    public record CreateInviteRequest(String email) {}
    public record InviteResponse(UUID id, UUID teamId, String token, String email, OffsetDateTime expiresAt) {}
}
""",

"src/main/java/com/dogfood/event/dto/EligibilityCheckResult.java": """package com.dogfood.event.dto;

import java.util.List;

public record EligibilityCheckResult(boolean eligible, List<String> violations) {}
""",

"src/main/java/com/dogfood/event/eligibility/EligibilityContext.java": """package com.dogfood.event.eligibility;

import java.util.UUID;

public record EligibilityContext(
    UUID teamId,
    UUID eventId,
    long teamSize,
    long existingSubmissionCount
) {}
""",

"src/main/java/com/dogfood/event/eligibility/EligibilityRule.java": """package com.dogfood.event.eligibility;

public interface EligibilityRule {
    String getType();
    boolean evaluate(EligibilityContext context);
    String getViolationMessage();
}
""",

"src/main/java/com/dogfood/event/eligibility/MaxTeamSizeRule.java": """package com.dogfood.event.eligibility;

public class MaxTeamSizeRule implements EligibilityRule {
    private final int maxSize;

    public MaxTeamSizeRule(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public String getType() {
        return "MAX_TEAM_SIZE";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.teamSize() <= maxSize;
    }

    @Override
    public String getViolationMessage() {
        return "Team size exceeds maximum allowed size of " + maxSize;
    }
}
""",

"src/main/java/com/dogfood/event/eligibility/MinTeamSizeRule.java": """package com.dogfood.event.eligibility;

public class MinTeamSizeRule implements EligibilityRule {
    private final int minSize;

    public MinTeamSizeRule(int minSize) {
        this.minSize = minSize;
    }

    @Override
    public String getType() {
        return "MIN_TEAM_SIZE";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.teamSize() >= minSize;
    }

    @Override
    public String getViolationMessage() {
        return "Team size is below minimum required size of " + minSize;
    }
}
""",

"src/main/java/com/dogfood/event/eligibility/OneSubmissionPerTeamRule.java": """package com.dogfood.event.eligibility;

public class OneSubmissionPerTeamRule implements EligibilityRule {

    @Override
    public String getType() {
        return "ONE_SUBMISSION_PER_TEAM";
    }

    @Override
    public boolean evaluate(EligibilityContext context) {
        return context.existingSubmissionCount() == 0;
    }

    @Override
    public String getViolationMessage() {
        return "Team already has a submission.";
    }
}
""",

"src/main/java/com/dogfood/event/eligibility/EligibilityEngine.java": """package com.dogfood.event.eligibility;

import com.dogfood.event.entity.Event;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EligibilityEngine {

    public List<String> evaluate(Event event, EligibilityContext context) {
        List<String> violations = new ArrayList<>();
        List<EligibilityRule> rules = parseRules(event.getEligibilityRules());
        
        for (EligibilityRule rule : rules) {
            if (!rule.evaluate(context)) {
                violations.add(rule.getViolationMessage());
            }
        }
        return violations;
    }

    private List<EligibilityRule> parseRules(List<Map<String, Object>> ruleConfigs) {
        List<EligibilityRule> rules = new ArrayList<>();
        if (ruleConfigs == null) return rules;

        for (Map<String, Object> config : ruleConfigs) {
            String type = (String) config.get("type");
            if (type == null) continue;
            
            switch (type) {
                case "MAX_TEAM_SIZE":
                    rules.add(new MaxTeamSizeRule(Integer.parseInt(config.get("value").toString())));
                    break;
                case "MIN_TEAM_SIZE":
                    rules.add(new MinTeamSizeRule(Integer.parseInt(config.get("value").toString())));
                    break;
                case "ONE_SUBMISSION_PER_TEAM":
                    rules.add(new OneSubmissionPerTeamRule());
                    break;
            }
        }
        return rules;
    }
}
""",

"src/main/java/com/dogfood/event/publisher/AuditPublisher.java": """package com.dogfood.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditPublisher {
    private final RabbitTemplate rabbitTemplate;
    // Assuming dogfood.audit exchange is defined in common config or manually
    private static final String EXCHANGE = "dogfood.audit";

    public void publishEvent(String eventType, UUID entityId, Map<String, Object> details) {
        try {
            Map<String, Object> payload = Map.of(
                "eventType", eventType,
                "entityId", entityId != null ? entityId.toString() : "",
                "details", details,
                "timestamp", OffsetDateTime.now()
            );
            rabbitTemplate.convertAndSend(EXCHANGE, eventType, payload);
            log.info("Published audit event: {} for entity: {}", eventType, entityId);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", eventType, e);
        }
    }
}
""",

"src/main/java/com/dogfood/event/service/EventService.java": """package com.dogfood.event.service;

import com.dogfood.event.dto.EventDtos.*;
import com.dogfood.event.entity.Event;
import com.dogfood.event.entity.EventStatus;
import com.dogfood.event.repository.EventRepository;
import com.dogfood.event.publisher.AuditPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final AuditPublisher auditPublisher;

    @Transactional
    public EventResponse createEvent(CreateEventRequest req, UUID organizerId) {
        String slug = generateSlug(req.name());
        Event event = Event.builder()
                .name(req.name())
                .slug(slug)
                .description(req.description())
                .bannerUrl(req.bannerUrl())
                .organizerId(organizerId)
                .submissionDeadline(req.submissionDeadline())
                .eligibilityRules(req.eligibilityRules())
                .customQuestions(req.customQuestions())
                .status(EventStatus.DRAFT)
                .build();

        event = eventRepository.save(event);
        auditPublisher.publishEvent("event.created", event.getId(), Map.of("name", event.getName()));
        
        return toResponse(event);
    }

    @Transactional
    public EventResponse updateEvent(UUID id, UpdateEventRequest req) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        
        if (req.name() != null) event.setName(req.name());
        if (req.description() != null) event.setDescription(req.description());
        if (req.submissionDeadline() != null) event.setSubmissionDeadline(req.submissionDeadline());
        if (req.status() != null) event.setStatus(req.status());
        if (req.eligibilityRules() != null) event.setEligibilityRules(req.eligibilityRules());
        if (req.customQuestions() != null) event.setCustomQuestions(req.customQuestions());

        event = eventRepository.save(event);
        auditPublisher.publishEvent("event.updated", event.getId(), Map.of("status", event.getStatus()));
        
        return toResponse(event);
    }

    public EventResponse getEvent(UUID id) {
        return eventRepository.findById(id).map(this::toResponse).orElseThrow();
    }
    
    public EventResponse getEventBySlug(String slug) {
        return eventRepository.findBySlug(slug).map(this::toResponse).orElseThrow();
    }

    private String generateSlug(String name) {
        String baseSlug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String slug = baseSlug;
        while (eventRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + UUID.randomUUID().toString().substring(0, 5);
        }
        return slug;
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
            event.getId(), event.getName(), event.getSlug(), event.getDescription(),
            event.getBannerUrl(), event.getOrganizerId(), event.getSubmissionDeadline(),
            event.getStatus(), event.getEligibilityRules(), event.getCustomQuestions(),
            event.getCreatedAt(), event.getUpdatedAt()
        );
    }
}
""",

"src/main/java/com/dogfood/event/service/TeamService.java": """package com.dogfood.event.service;

import com.dogfood.event.dto.TeamDtos.*;
import com.dogfood.event.entity.*;
import com.dogfood.event.repository.*;
import com.dogfood.event.publisher.AuditPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamInviteRepository teamInviteRepository;
    private final EventRepository eventRepository;
    private final AuditPublisher auditPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public TeamResponse createTeam(UUID eventId, CreateTeamRequest req, UUID userId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        
        Team team = Team.builder()
                .event(event)
                .name(req.name())
                .createdBy(userId)
                .build();
        team = teamRepository.save(team);
        
        TeamMember member = TeamMember.builder()
                .team(team)
                .userId(userId)
                .role(TeamRole.LEADER)
                .build();
        teamMemberRepository.save(member);
        
        auditPublisher.publishEvent("team.created", team.getId(), Map.of("eventId", eventId));
        return new TeamResponse(team.getId(), eventId, team.getName(), team.getCreatedBy(), team.getCreatedAt());
    }

    @Transactional
    public InviteResponse createInvite(UUID teamId, CreateInviteRequest req) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        TeamInvite invite = TeamInvite.builder()
                .team(team)
                .token(token)
                .email(req.email())
                .expiresAt(OffsetDateTime.now().plusDays(7))
                .accepted(false)
                .build();
        invite = teamInviteRepository.save(invite);
        
        auditPublisher.publishEvent("team.invite.created", teamId, Map.of("email", req.email() != null ? req.email() : "link"));
        return new InviteResponse(invite.getId(), teamId, token, invite.getEmail(), invite.getExpiresAt());
    }

    @Transactional
    public TeamMemberResponse joinTeam(String token, UUID userId) {
        TeamInvite invite = teamInviteRepository.findByToken(token).orElseThrow();
        if (invite.getAccepted() || invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Invite invalid or expired");
        }
        
        Team team = invite.getTeam();
        if (!teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userId)) {
            TeamMember member = TeamMember.builder()
                    .team(team)
                    .userId(userId)
                    .role(TeamRole.MEMBER)
                    .build();
            teamMemberRepository.save(member);
            auditPublisher.publishEvent("team.member.joined", team.getId(), Map.of("userId", userId));
        }
        
        invite.setAccepted(true);
        teamInviteRepository.save(invite);
        
        return new TeamMemberResponse(null, team.getId(), userId, TeamRole.MEMBER, OffsetDateTime.now());
    }
}
""",

"src/main/java/com/dogfood/event/service/EligibilityService.java": """package com.dogfood.event.service;

import com.dogfood.event.dto.EligibilityCheckResult;
import com.dogfood.event.eligibility.EligibilityContext;
import com.dogfood.event.eligibility.EligibilityEngine;
import com.dogfood.event.entity.Event;
import com.dogfood.event.repository.EventRepository;
import com.dogfood.event.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final EventRepository eventRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EligibilityEngine eligibilityEngine;

    public EligibilityCheckResult checkTeamEligibility(UUID eventId, UUID teamId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        long teamSize = teamMemberRepository.countByTeamId(teamId);
        // Assuming submissions check is external or mocked for now
        long submissions = 0; 

        EligibilityContext context = new EligibilityContext(teamId, eventId, teamSize, submissions);
        List<String> violations = eligibilityEngine.evaluate(event, context);

        return new EligibilityCheckResult(violations.isEmpty(), violations);
    }
}
""",

"src/main/java/com/dogfood/event/controller/EventController.java": """package com.dogfood.event.controller;

import com.dogfood.event.dto.EventDtos.*;
import com.dogfood.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create an event")
    public EventResponse createEvent(@RequestBody @Valid CreateEventRequest request, 
                                     @RequestHeader("X-User-Id") UUID userId) {
        return eventService.createEvent(request, userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event")
    public EventResponse updateEvent(@PathVariable UUID id, @RequestBody @Valid UpdateEventRequest request) {
        return eventService.updateEvent(id, request);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get an event by slug")
    public EventResponse getEventBySlug(@PathVariable String slug) {
        return eventService.getEventBySlug(slug);
    }
}
""",

"src/main/java/com/dogfood/event/controller/TrackController.java": """package com.dogfood.event.controller;

import com.dogfood.event.dto.EventDtos.*;
import com.dogfood.event.entity.Event;
import com.dogfood.event.entity.Track;
import com.dogfood.event.repository.EventRepository;
import com.dogfood.event.repository.TrackRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackRepository trackRepository;
    private final EventRepository eventRepository;

    @PostMapping
    @Operation(summary = "Create a track for an event")
    public TrackResponse createTrack(@PathVariable UUID eventId, @RequestBody @Valid CreateTrackRequest req) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        Track track = Track.builder()
                .event(event)
                .name(req.name())
                .description(req.description())
                .sortOrder(req.sortOrder())
                .build();
        track = trackRepository.save(track);
        return new TrackResponse(track.getId(), eventId, track.getName(), track.getDescription(), track.getSortOrder());
    }

    @GetMapping
    @Operation(summary = "List tracks for an event")
    public List<TrackResponse> listTracks(@PathVariable UUID eventId) {
        return trackRepository.findByEventId(eventId).stream()
                .map(t -> new TrackResponse(t.getId(), eventId, t.getName(), t.getDescription(), t.getSortOrder()))
                .toList();
    }
}
""",

"src/main/java/com/dogfood/event/controller/TeamController.java": """package com.dogfood.event.controller;

import com.dogfood.event.dto.TeamDtos.*;
import com.dogfood.event.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/events/{eventId}/teams")
    @Operation(summary = "Create a team")
    public TeamResponse createTeam(@PathVariable UUID eventId, 
                                   @RequestBody @Valid CreateTeamRequest request,
                                   @RequestHeader("X-User-Id") UUID userId) {
        return teamService.createTeam(eventId, request, userId);
    }

    @PostMapping("/teams/{teamId}/invite")
    @Operation(summary = "Create team invite link")
    public InviteResponse createInvite(@PathVariable UUID teamId, 
                                       @RequestBody @Valid CreateInviteRequest request) {
        return teamService.createInvite(teamId, request);
    }

    @PostMapping("/teams/join/{token}")
    @Operation(summary = "Join a team using an invite token")
    public TeamMemberResponse joinTeam(@PathVariable String token, 
                                       @RequestHeader("X-User-Id") UUID userId) {
        return teamService.joinTeam(token, userId);
    }
}
"""
}

for path, content in files.items():
    full_path = os.path.join(base_dir, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, 'w') as f:
        f.write(content)

print(f"Generated {len(files)} files successfully.")
