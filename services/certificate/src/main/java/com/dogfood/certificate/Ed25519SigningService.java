package com.dogfood.certificate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class Ed25519SigningService {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final String KEYS_DIR = "/app/keys";

    @PostConstruct
    public void init() throws Exception {
        File pubFile = new File(KEYS_DIR, "public.key");
        File privFile = new File(KEYS_DIR, "private.key");
        
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        if (pubFile.exists() && privFile.exists()) {
            byte[] pubBytes = Base64.getDecoder().decode(Files.readString(pubFile.toPath()));
            byte[] privBytes = Base64.getDecoder().decode(Files.readString(privFile.toPath()));
            publicKey = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            log.info("Loaded existing Ed25519 keys.");
        } else {
            new File(KEYS_DIR).mkdirs();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
            KeyPair kp = kpg.generateKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
            Files.writeString(pubFile.toPath(), Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            Files.writeString(privFile.toPath(), Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            log.info("Generated new Ed25519 keys.");
        }
    }

    public String sign(byte[] data) throws Exception {
        Signature sig = Signature.getInstance("Ed25519");
        sig.initSign(privateKey);
        sig.update(data);
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public boolean verify(byte[] data, String signatureBase64) throws Exception {
        Signature sig = Signature.getInstance("Ed25519");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(Base64.getDecoder().decode(signatureBase64));
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
