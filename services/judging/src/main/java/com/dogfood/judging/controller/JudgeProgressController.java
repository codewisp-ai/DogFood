package com.dogfood.judging.controller;

import com.dogfood.common.events.RabbitConstants;
import com.dogfood.common.events.ScoreSubmittedEvent;
import com.dogfood.judging.entity.JudgeAssignment;
import com.dogfood.judging.repository.JudgeAssignmentRepository;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/events/{eventId}/judge-progress")
public class JudgeProgressController {

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final JudgeAssignmentRepository assignmentRepo;

    public JudgeProgressController(JudgeAssignmentRepository assignmentRepo) {
        this.assignmentRepo = assignmentRepo;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(
            @PathVariable UUID eventId,
            jakarta.servlet.http.HttpServletRequest request) {
        if (!com.dogfood.common.security.RequestContext.isOrganizer(request) && 
            request.getHeader(com.dogfood.common.security.RequestContext.HEADER_USER_ROLES) != null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Only organizers can view judge progress telemetry");
        }

        SseEmitter emitter = new SseEmitter(600000L); // 10 min timeout
        emitters.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            List<SseEmitter> list = emitters.get(eventId);
            if (list != null) list.remove(emitter);
        });
        emitter.onTimeout(() -> {
            List<SseEmitter> list = emitters.get(eventId);
            if (list != null) list.remove(emitter);
        });
        emitter.onError((e) -> {
            List<SseEmitter> list = emitters.get(eventId);
            if (list != null) list.remove(emitter);
        });

        // Send initial state immediately
        broadcastProgress(eventId, List.of(emitter));

        return emitter;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue, // anonymous auto-delete queue
            exchange = @Exchange(value = RabbitConstants.SCORES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitConstants.SCORE_SUBMITTED
    ))
    public void onScoreSubmitted(ScoreSubmittedEvent event) {
        List<SseEmitter> eventEmitters = emitters.get(event.eventId());
        if (eventEmitters != null && !eventEmitters.isEmpty()) {
            broadcastProgress(event.eventId(), eventEmitters);
        }
    }

    private void broadcastProgress(UUID eventId, List<SseEmitter> targetEmitters) {
        List<JudgeAssignment> assignments = assignmentRepo.findByEventId(eventId);
        long completed = assignments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long total = assignments.size();
        
        Map<UUID, List<JudgeAssignment>> byJudge = assignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(JudgeAssignment::getJudgeId));

        List<Map<String, Object>> judgeStats = byJudge.entrySet().stream()
                .map(entry -> {
                    UUID jId = entry.getKey();
                    List<JudgeAssignment> jAssignments = entry.getValue();
                    long jCompleted = jAssignments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
                    long jInProgress = jAssignments.stream().filter(a -> "IN_PROGRESS".equals(a.getStatus())).count();
                    long jTotal = jAssignments.size();
                    String status = (jCompleted == 0 && jInProgress == 0) ? "NOT_STARTED" :
                                    (jCompleted == jTotal && jTotal > 0) ? "COMPLETED" : "IN_PROGRESS";
                    double pct = jTotal > 0 ? (jCompleted * 100.0 / jTotal) : 0.0;
                    return Map.<String, Object>of(
                            "judgeId", jId.toString(),
                            "totalAssigned", jTotal,
                            "completedReviews", jCompleted,
                            "percentage", Math.round(pct),
                            "status", status
                    );
                })
                .toList();

        Map<String, Object> payload = Map.of(
                "completed", completed,
                "total", total,
                "percentage", total > 0 ? Math.round(completed * 100.0 / total) : 0,
                "judges", judgeStats
        );

        for (SseEmitter emitter : targetEmitters) {
            try {
                emitter.send(payload, MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                // Let onCompletion handle cleanup
            }
        }
    }
}
