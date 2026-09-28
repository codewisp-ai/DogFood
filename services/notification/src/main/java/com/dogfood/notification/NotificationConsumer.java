package com.dogfood.notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final NotificationService service;

    @RabbitListener(queues = "notification.events")
    public void handleNotificationEvent(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString(event.get("userId").toString());
            String title = (String) event.get("title");
            String message = (String) event.get("message");
            service.createNotification(userId, title, message);
        } catch (Exception e) {
            log.error("Failed to process notification event", e);
        }
    }
}
