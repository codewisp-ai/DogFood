package com.dogfood.event.controller;

import com.dogfood.event.entity.Event;
import com.dogfood.event.entity.Prize;
import com.dogfood.event.entity.Track;
import com.dogfood.event.repository.EventRepository;
import com.dogfood.event.repository.PrizeRepository;
import com.dogfood.event.repository.TrackRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/prizes")
@RequiredArgsConstructor
public class PrizeController {

    private final PrizeRepository prizeRepository;
    private final EventRepository eventRepository;
    private final TrackRepository trackRepository;

    public record CreatePrizeRequest(@NotBlank String name, String description, UUID trackId, Integer sortOrder) {}
    public record PrizeResponse(UUID id, UUID eventId, UUID trackId, String name, String description, Integer sortOrder) {}

    @PostMapping
    @Operation(summary = "Create a prize for an event")
    public ResponseEntity<PrizeResponse> createPrize(
            @PathVariable UUID eventId,
            @RequestBody CreatePrizeRequest req) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        Track track = req.trackId() != null ? trackRepository.findById(req.trackId()).orElse(null) : null;
        Prize prize = Prize.builder()
                .event(event)
                .track(track)
                .name(req.name())
                .description(req.description())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .build();
        prize = prizeRepository.save(prize);
        return ResponseEntity.ok(toResponse(prize));
    }

    @GetMapping
    @Operation(summary = "List prizes for an event")
    public List<PrizeResponse> listPrizes(@PathVariable UUID eventId) {
        return prizeRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{prizeId}")
    @Operation(summary = "Delete a prize")
    public ResponseEntity<Void> deletePrize(@PathVariable UUID prizeId) {
        prizeRepository.deleteById(prizeId);
        return ResponseEntity.noContent().build();
    }

    private PrizeResponse toResponse(Prize p) {
        return new PrizeResponse(
                p.getId(),
                p.getEvent().getId(),
                p.getTrack() != null ? p.getTrack().getId() : null,
                p.getName(),
                p.getDescription(),
                p.getSortOrder()
        );
    }
}
