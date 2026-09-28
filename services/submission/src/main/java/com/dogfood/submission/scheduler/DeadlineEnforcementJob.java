package com.dogfood.submission.scheduler;

import com.dogfood.submission.client.EventServiceClient;
import com.dogfood.submission.entity.Submission;
import com.dogfood.submission.entity.SubmissionStatus;
import com.dogfood.submission.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeadlineEnforcementJob {

    private final SubmissionRepository submissionRepository;
    private final EventServiceClient eventServiceClient;
    private final StringRedisTemplate redisTemplate;

    private static final String LOCK_KEY = "lock:deadline_enforcement";

    @Scheduled(fixedRate = 60000)
    public void enforceDeadlines() {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "locked", Duration.ofSeconds(50));
        if (Boolean.TRUE.equals(acquired)) {
            try {
                log.info("Running deadline enforcement job...");
                List<Submission> drafts = submissionRepository.findByStatus(SubmissionStatus.DRAFT);
                
                int autoSubmitted = 0;
                for (Submission draft : drafts) {
                    Instant deadline = eventServiceClient.getEventDeadline(draft.getEventId());
                    if (Instant.now().isAfter(deadline)) {
                        draft.setStatus(SubmissionStatus.SUBMITTED);
                        draft.setSubmittedAt(Instant.now());
                        submissionRepository.save(draft);
                        autoSubmitted++;
                    }
                }
                if (autoSubmitted > 0) {
                    log.info("Auto-submitted {} drafts.", autoSubmitted);
                }
            } finally {
                redisTemplate.delete(LOCK_KEY);
            }
        }
    }
}
