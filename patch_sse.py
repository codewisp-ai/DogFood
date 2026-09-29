import sys

new_controller = """package com.dogfood.judging.controller;

import com.dogfood.common.events.RabbitConstants;
import com.dogfood.common.events.ScoreSubmittedEvent;
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable UUID eventId) {
        SseEmitter emitter = new SseEmitter(600000L); // 10 min timeout
        emitters.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.get(eventId).remove(emitter));
        emitter.onTimeout(() -> emitters.get(eventId).remove(emitter));
        emitter.onError((e) -> emitters.get(eventId).remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (Exception e) {}

        return emitter;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue, // anonymous auto-delete queue
            exchange = @Exchange(value = RabbitConstants.SCORES_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = RabbitConstants.SCORE_SUBMITTED
    ))
    public void onScoreSubmitted(ScoreSubmittedEvent event) {
        List<SseEmitter> eventEmitters = emitters.get(event.eventId());
        if (eventEmitters != null) {
            for (SseEmitter emitter : eventEmitters) {
                try {
                    // Send minimal signal so client can refetch the progress payload
                    emitter.send(SseEmitter.event().name("progress-update").data(event.judgeId().toString()));
                } catch (Exception e) {
                    // cleanup handled by onCompletion
                }
            }
        }
    }
}
"""

with open('services/judging/src/main/java/com/dogfood/judging/controller/JudgeProgressController.java', 'w') as f:
    f.write(new_controller)

