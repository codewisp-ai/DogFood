package com.dogfood.event.controller;

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
