package com.dogfood.event.service;

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
