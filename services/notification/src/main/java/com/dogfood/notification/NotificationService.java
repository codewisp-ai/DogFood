package com.dogfood.notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository repository;

    @Transactional
    public void createNotification(UUID userId, String title, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setMessage(message);
        repository.save(n);
        log.info("Email Stub: Sent email to userId={} with title='{}'", userId, title);
    }

    public List<Notification> getNotifications(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(UUID userId) {
        return repository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        repository.findById(id).ifPresent(n -> {
            if(n.getUserId().equals(userId)) {
                n.setRead(true);
                repository.save(n);
            }
        });
    }
}
