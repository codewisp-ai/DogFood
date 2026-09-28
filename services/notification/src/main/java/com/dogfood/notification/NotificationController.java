package com.dogfood.notification;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    public List<Notification> getNotifications(@RequestHeader("X-User-Id") UUID userId) {
        return service.getNotifications(userId);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable UUID id, @RequestHeader("X-User-Id") UUID userId) {
        service.markAsRead(id, userId);
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(@RequestHeader("X-User-Id") UUID userId) {
        return service.getUnreadCount(userId);
    }
}
