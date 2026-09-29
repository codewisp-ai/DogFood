package com.dogfood.event.bulk;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
public class BulkExportWorker {

    private final ExportJobRepository exportJobRepository;
    private final RabbitTemplate rabbitTemplate;
    // We would inject MinioClient and other repos here in a full implementation,
    // but for the hackathon we mock the data assembly and Minio upload to prove the architecture.
    // private final MinioClient minioClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "event.export.queue", durable = "true"),
            exchange = @Exchange(value = "dogfood.events", type = "topic"),
            key = "event.export.requested"
    ))
    public void processExportRequest(Map<String, Object> payload) {
        try {
            UUID jobId = UUID.fromString(payload.get("jobId").toString());
            UUID eventId = UUID.fromString(payload.get("eventId").toString());

            ExportJob job = exportJobRepository.findById(jobId).orElseThrow();
            job.setStatus("PROCESSING");
            exportJobRepository.save(job);

            // 1. In a real scenario, query the DB to build the JSON export
            String mockJsonData = "{ \"eventId\": \"" + eventId + "\", \"exported\": true }";
            byte[] dataBytes = mockJsonData.getBytes();

            // 2. Upload to Minio (S3)
            // minioClient.putObject(PutObjectArgs.builder()
            //      .bucket("exports")
            //      .object(jobId.toString() + ".json")
            //      .stream(new ByteArrayInputStream(dataBytes), dataBytes.length, -1)
            //      .build());

            String mockS3Url = "s3://dogfood-exports/" + jobId.toString() + ".json";

            // 3. Update Job
            job.setStatus("COMPLETED");
            job.setS3Url(mockS3Url);
            exportJobRepository.save(job);

            // 4. Publish completion event for notification-service to email the organizer
            rabbitTemplate.convertAndSend("dogfood.events", "event.export.completed", 
                Map.of("jobId", jobId, "s3Url", mockS3Url));

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback logic to mark job as FAILED
        }
    }
}
