package com.dogfood.submission.controller;

import com.dogfood.common.dto.PageResponse;
import com.dogfood.submission.dto.CreateSubmissionRequest;
import com.dogfood.submission.dto.GalleryFilter;
import com.dogfood.submission.dto.SubmissionResponse;
import com.dogfood.submission.dto.UpdateSubmissionRequest;
import com.dogfood.submission.service.SubmissionService;
import com.dogfood.submission.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final StorageService storageService;
    private final com.dogfood.submission.repository.CommentRepository commentRepository;

    @PostMapping("/submissions")
    public SubmissionResponse createOrUpdateSubmission(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreateSubmissionRequest request) {
        return submissionService.createOrUpdate(request, userId != null ? UUID.fromString(userId) : null);
    }

    @PatchMapping("/submissions/{id}")
    public SubmissionResponse updateSubmission(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody UpdateSubmissionRequest request) {
        return submissionService.update(id, request, userId != null ? UUID.fromString(userId) : null);
    }

    @GetMapping("/submissions/{id}")
    public SubmissionResponse getSubmission(@PathVariable UUID id) {
        return submissionService.getById(id);
    }

    @PostMapping("/submissions/{id}/submit")
    public SubmissionResponse submitFinal(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return submissionService.submitFinal(id, userId != null ? UUID.fromString(userId) : null);
    }

    @GetMapping("/events/{eventId}/gallery")
    public PageResponse<SubmissionResponse> getGallery(
            @PathVariable UUID eventId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID trackId,
            @RequestParam(required = false) List<String> tags,
            Pageable pageable) {
        GalleryFilter filter = new GalleryFilter(search, trackId, tags);
        Page<SubmissionResponse> page = submissionService.searchGallery(eventId, filter, pageable);
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @PostMapping("/submissions/{id}/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = storageService.uploadFile(file, "submissions/" + id.toString());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping(value = "/events/{eventId}/submissions/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportSubmissionsCsv(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        if (roles == null || !roles.contains("ORGANIZER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        GalleryFilter filter = new GalleryFilter(null, null, null);
        Page<SubmissionResponse> page = submissionService.searchGallery(eventId, filter, Pageable.unpaged());

        StringBuilder csv = new StringBuilder();
        csv.append("Id,Name,Tagline,TrackId,RepositoryUrl,DemoVideoUrl,LiveLink,Status,RiskScore\n");
        for (SubmissionResponse s : page.getContent()) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    s.id(),
                    escapeCsv(s.name()),
                    escapeCsv(s.tagline()),
                    s.trackId(),
                    escapeCsv(s.repositoryUrl()),
                    escapeCsv(s.demoVideoUrl()),
                    escapeCsv(s.liveLink()),
                    s.status(),
                    s.riskScore() != null ? s.riskScore() : 0.0
            ));
        }
        return ResponseEntity.ok(csv.toString());
    }

    // --- Comments ---

    public record CreateCommentRequest(String content) {}

    @PostMapping("/submissions/{id}/comments")
    public com.dogfood.submission.entity.Comment postComment(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "Anonymous") String userName,
            @RequestBody CreateCommentRequest request) {

        com.dogfood.submission.entity.Comment comment = new com.dogfood.submission.entity.Comment();
        comment.setSubmissionId(id);
        comment.setAuthorId(userId != null ? UUID.fromString(userId) : UUID.randomUUID());
        comment.setAuthorName(userName != null ? userName : "Anonymous");
        comment.setContent(request.content());

        return commentRepository.save(comment);
    }

    @GetMapping("/submissions/{id}/comments")
    public List<com.dogfood.submission.entity.Comment> getComments(@PathVariable UUID id) {
        return commentRepository.findBySubmissionIdOrderByCreatedAtDesc(id);
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escaped = data.replaceAll("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
