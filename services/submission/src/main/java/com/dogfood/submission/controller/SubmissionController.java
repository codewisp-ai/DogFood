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

    @GetMapping("/events/{eventId}/submissions/export")
    public ResponseEntity<List<SubmissionResponse>> exportSubmissions(
            @PathVariable UUID eventId,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        // TODO: check ORGANIZER role
        GalleryFilter filter = new GalleryFilter(null, null, null);
        Page<SubmissionResponse> page = submissionService.searchGallery(eventId, filter, Pageable.unpaged());
        return ResponseEntity.ok(page.getContent());
    }
}
