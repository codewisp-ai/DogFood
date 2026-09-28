package com.dogfood.webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookEndpointRepository endpointRepo;
    private final WebhookDeliveryRepository deliveryRepo;

    @PostMapping("/endpoints")
    public WebhookEndpoint registerEndpoint(@RequestBody WebhookEndpoint endpoint, @RequestHeader("X-User-Roles") String roles) {
        if (!roles.contains("ORGANIZER")) { throw new RuntimeException("Must be ORGANIZER"); }
        return endpointRepo.save(endpoint);
    }

    @GetMapping("/endpoints")
    public List<WebhookEndpoint> getEndpoints(@RequestParam UUID eventId) {
        return endpointRepo.findByEventIdAndActiveTrue(eventId);
    }

    @DeleteMapping("/endpoints/{id}")
    public void deleteEndpoint(@PathVariable UUID id) {
        endpointRepo.findById(id).ifPresent(ep -> {
            ep.setActive(false);
            endpointRepo.save(ep);
        });
    }

    @GetMapping("/deliveries")
    public List<WebhookDelivery> getDeliveries(@RequestParam UUID endpointId) {
        return deliveryRepo.findByEndpointIdOrderByCreatedAtDesc(endpointId);
    }
}
