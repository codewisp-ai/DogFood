package com.dogfood.certificate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {
    private final Ed25519SigningService signingService;
    private final MinioClient minioClient;

    @GetMapping("/{eventId}/{userId}")
    public ResponseEntity<byte[]> getCertificate(@PathVariable UUID eventId, @PathVariable UUID userId) throws Exception {
        var stream = minioClient.getObject(GetObjectArgs.builder()
            .bucket("certificates")
            .object(eventId + "/" + userId + ".pdf")
            .build());
        byte[] bytes = stream.readAllBytes();
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .body(bytes);
    }

    @GetMapping("/verify")
    public boolean verify(@RequestBody Map<String, String> request) throws Exception {
        byte[] data = java.util.Base64.getDecoder().decode(request.get("dataBase64"));
        return signingService.verify(data, request.get("signature"));
    }

    @GetMapping("/.well-known/public-key")
    public String getPublicKey() {
        return signingService.getPublicKeyBase64();
    }
}
