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
    public SseEmitter streamProgress(@PathVariable UUID eventId) {
        SseEmitter emitter = new SseEmitter(600000L); // 10 min timeout
        emitters.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.get(eventId).remove(emitter));
        emitter.onTimeout(() -> emitters.get(eventId).remove(emitter));
        emitter.onError((e) -> emitters.get(eventId).remove(emitter));

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
        
        Map<String, Object> payload = Map.of(
                "completed", completed,
                "total", total,
                "judges", List.of() // empty for now as frontend doesn't render it yet
        );

        for (SseEmitter emitter : targetEmitters) {
            try {
                // Send plain data, frontend's onmessage will catch it
                emitter.send(payload, MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                // Let onCompletion handle cleanup
            }
        }
    }
}
