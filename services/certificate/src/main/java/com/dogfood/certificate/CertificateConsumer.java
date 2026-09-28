package com.dogfood.certificate;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateConsumer {
    private final CertificateGenerator generator;
    private final Ed25519SigningService signingService;
    private final MinioClient minioClient;
    private final String BUCKET = "certificates";

    @RabbitListener(queues = "certificate.generate")
    public void processEvent(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString(event.get("userId").toString());
            UUID eventId = UUID.fromString(event.get("eventId").toString());
            String recipientName = (String) event.get("recipientName");
            String eventName = (String) event.get("eventName");
            String track = (String) event.get("track");
            String rank = (String) event.get("rank");
            UUID certId = UUID.randomUUID();
            
            byte[] pdfBytes = generator.generate(recipientName, eventName, track, rank, certId);
            String signature = signingService.sign(pdfBytes);
            
            String objectName = eventId + "/" + userId + ".pdf";
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .object(objectName)
                .stream(new ByteArrayInputStream(pdfBytes), pdfBytes.length, -1)
                .contentType("application/pdf")
                .userMetadata(Map.of("Signature", signature))
                .build());
                
            log.info("Certificate {} generated and uploaded for user {}", certId, userId);
        } catch (Exception e) {
            log.error("Failed to process certificate generation", e);
        }
    }
}
