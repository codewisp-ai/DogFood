package com.dogfood.event.controller;

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
