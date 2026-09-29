package com.dogfood.submission.service;

import com.dogfood.common.events.AuditEvent;
import com.dogfood.submission.client.EventServiceClient;
import com.dogfood.submission.dto.CreateSubmissionRequest;
import com.dogfood.submission.dto.GalleryFilter;
import com.dogfood.submission.dto.SubmissionResponse;
import com.dogfood.submission.dto.UpdateSubmissionRequest;
import com.dogfood.submission.entity.Submission;
import com.dogfood.submission.entity.SubmissionStatus;
import com.dogfood.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final EventServiceClient eventServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final GitForensicService gitForensicService;

    @Transactional
    public SubmissionResponse createOrUpdate(CreateSubmissionRequest request, UUID userId) {
        checkDeadline(request.eventId());
        // TODO: check team membership

        Submission submission = submissionRepository.findByEventIdAndTeamId(request.eventId(), request.teamId())
                .orElseGet(() -> Submission.builder()
                        .eventId(request.eventId())
                        .teamId(request.teamId())
                        .build());

        submission.setTrackId(request.trackId());
        submission.setName(request.name());
        submission.setTagline(request.tagline());
        submission.setDescription(request.description());
        submission.setThumbnailUrl(request.thumbnailUrl());
        submission.setImageGallery(request.imageGallery() != null ? request.imageGallery() : List.of());
        submission.setDemoVideoUrl(request.demoVideoUrl());
        submission.setRepositoryUrl(request.repositoryUrl());
        submission.setLiveLink(request.liveLink());
        submission.setTechTags(request.techTags() != null ? request.techTags() : List.of());
        submission.setCustomAnswers(request.customAnswers() != null ? request.customAnswers() : Map.of());

        boolean isNew = submission.getId() == null;
        submission = submissionRepository.save(submission);

        if (submission.getRepositoryUrl() != null && !submission.getRepositoryUrl().isEmpty()) {
            Instant eventStart = eventServiceClient.getEventStartDate(submission.getEventId());
            gitForensicService.analyzeRepository(submission.getId(), submission.getRepositoryUrl(), eventStart);
        }

        publishEvent(isNew ? "submission.created" : "submission.updated", submission.getId(), userId);

        return mapToResponse(submission);
    }

    @Transactional
    public SubmissionResponse update(UUID id, UpdateSubmissionRequest request, UUID userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        checkDeadline(submission.getEventId());
        
        if (request.trackId() != null) submission.setTrackId(request.trackId());
        if (request.name() != null) submission.setName(request.name());
        if (request.tagline() != null) submission.setTagline(request.tagline());
        if (request.description() != null) submission.setDescription(request.description());
        if (request.thumbnailUrl() != null) submission.setThumbnailUrl(request.thumbnailUrl());
        if (request.imageGallery() != null) submission.setImageGallery(request.imageGallery());
        if (request.demoVideoUrl() != null) submission.setDemoVideoUrl(request.demoVideoUrl());
        if (request.repositoryUrl() != null) submission.setRepositoryUrl(request.repositoryUrl());
        if (request.liveLink() != null) submission.setLiveLink(request.liveLink());
        if (request.techTags() != null) submission.setTechTags(request.techTags());
        if (request.customAnswers() != null) submission.setCustomAnswers(request.customAnswers());

        submission = submissionRepository.save(submission);
        
        if (request.repositoryUrl() != null && !request.repositoryUrl().isEmpty()) {
            Instant eventStart = eventServiceClient.getEventStartDate(submission.getEventId());
            gitForensicService.analyzeRepository(submission.getId(), submission.getRepositoryUrl(), eventStart);
        }
        
        publishEvent("submission.updated", submission.getId(), userId);
        return mapToResponse(submission);
    }

    @Transactional
    public SubmissionResponse submitFinal(UUID id, UUID userId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

        checkDeadline(submission.getEventId());

        if (submission.getStatus() != SubmissionStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submission is already submitted");
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(Instant.now());
        submission = submissionRepository.save(submission);

        publishEvent("submission.submitted", submission.getId(), userId);

        return mapToResponse(submission);
    }

    public SubmissionResponse getById(UUID id) {
        return submissionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));
    }

    public List<SubmissionResponse> getMySubmissions(UUID eventId, UUID userId) {
        List<UUID> teamIds = eventServiceClient.getUserTeamIds(eventId, userId);
        if (teamIds.isEmpty()) return List.of();
        return submissionRepository.findByEventIdAndTeamIdIn(eventId, teamIds).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<SubmissionResponse> searchGallery(UUID eventId, GalleryFilter filter, Pageable pageable) {
        String tagsStr = filter.tags() != null && !filter.tags().isEmpty() ? 
                String.join(",", filter.tags()) : null;

        Page<Submission> submissions = submissionRepository.searchGallery(
                eventId,
                filter.search(),
                filter.trackId(),
                tagsStr,
                pageable
        );

        return submissions.map(this::mapToResponse);
    }

    private void checkDeadline(UUID eventId) {
        Instant deadline = eventServiceClient.getEventDeadline(eventId);
        if (Instant.now().isAfter(deadline)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submission deadline has passed");
        }
    }

    private void publishEvent(String action, UUID entityId, UUID userId) {
        AuditEvent event = AuditEvent.of(
                action,
                "submission",
                entityId,
                userId,
                "SYSTEM", // Role
                com.dogfood.common.security.RequestContext.getCorrelationId() // Correlation ID
        );
        rabbitTemplate.convertAndSend("dogfood.audit", action, event);
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getEventId(),
                submission.getTeamId(),
                submission.getTrackId(),
                submission.getName(),
                submission.getTagline(),
                submission.getDescription(),
                submission.getThumbnailUrl(),
                submission.getImageGallery(),
                submission.getDemoVideoUrl(),
                submission.getRepositoryUrl(),
                submission.getLiveLink(),
                submission.getTechTags(),
                submission.getCustomAnswers(),
                submission.getStatus(),
                submission.getForensicStatus(),
                submission.getRiskScore(),
                submission.getRiskFlags(),
                submission.getSubmittedAt(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}
