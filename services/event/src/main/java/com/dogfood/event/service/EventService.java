package com.dogfood.event.service;

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

    public EventResponse getEventByIdOrSlug(String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return eventRepository.findById(id).map(this::toResponse)
                    .orElseGet(() -> eventRepository.findBySlug(identifier).map(this::toResponse).orElseThrow());
        } catch (IllegalArgumentException e) {
            return eventRepository.findBySlug(identifier).map(this::toResponse).orElseThrow();
        }
    }

    public java.util.List<EventResponse> listEvents() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
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
