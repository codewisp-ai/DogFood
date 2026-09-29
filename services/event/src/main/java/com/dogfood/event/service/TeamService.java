package com.dogfood.event.service;

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

    public java.util.List<TeamResponse> getTeamsByEvent(UUID eventId) {
        return teamRepository.findByEventId(eventId).stream()
                .map(t -> new TeamResponse(t.getId(), eventId, t.getName(), t.getCreatedBy(), t.getCreatedAt()))
                .toList();
    }

    public TeamResponse getTeamById(UUID teamId) {
        Team t = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team not found"));
        return new TeamResponse(t.getId(), t.getEvent().getId(), t.getName(), t.getCreatedBy(), t.getCreatedAt());
    }

    public java.util.List<TeamMemberResponse> getTeamMembers(UUID teamId) {
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(m -> new TeamMemberResponse(m.getId(), teamId, m.getUserId(), m.getRole(), m.getJoinedAt()))
                .toList();
    }
}
