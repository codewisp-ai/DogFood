package com.dogfood.event.controller;

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
