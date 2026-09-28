package com.dogfood.webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import java.time.ZonedDateTime;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryConsumer {
    private final WebhookEndpointRepository endpointRepo;
    private final WebhookDeliveryRepository deliveryRepo;
    private final HmacSigner signer;
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @RabbitListener(queues = "webhook.delivery")
    public void processEvent(Map<String, Object> event) {
        try {
            UUID eventId = UUID.fromString(event.get("eventId").toString());
            String payload = mapper.writeValueAsString(event);
            List<WebhookEndpoint> endpoints = endpointRepo.findByEventIdAndActiveTrue(eventId);
            for (WebhookEndpoint ep : endpoints) {
                WebhookDelivery delivery = new WebhookDelivery();
                delivery.setEndpointId(ep.getId());
                delivery.setPayload(payload);
                deliveryRepo.save(delivery);
                deliver(delivery, ep, payload);
            }
        } catch (Exception e) {
            log.error("Failed to parse/process webhook event", e);
        }
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void deliver(WebhookDelivery delivery, WebhookEndpoint ep, String payload) throws Exception {
        try {
            delivery.setAttempts(delivery.getAttempts() + 1);
            delivery.setLastAttemptAt(ZonedDateTime.now());
            
            String signature = signer.sign(payload, ep.getSecret());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Dogfood-Signature", signature);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(ep.getUrl(), request, String.class);
            delivery.setResponseCode(response.getStatusCode().value());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                delivery.setStatus("SUCCESS");
                deliveryRepo.save(delivery);
            } else {
                throw new RuntimeException("Non-2xx response");
            }
        } catch (Exception e) {
            delivery.setErrorMessage(e.getMessage());
            delivery.setStatus("FAILED");
            deliveryRepo.save(delivery);
            if (delivery.getAttempts() >= 5) {
                delivery.setStatus("DLQ");
                deliveryRepo.save(delivery);
                throw new AmqpRejectAndDontRequeueException("Max retries reached");
            }
            throw e;
        }
    }
}
