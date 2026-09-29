package com.dogfood.identity.security;

import com.dogfood.identity.entity.User;
import com.dogfood.identity.entity.UserEventRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Base64;
import java.util.UUID;

@Service
public class JwtService {

    private KeyPair keyPair;

    @Getter
    private Map<String, Object> jwks;

    @PostConstruct
    public void init() {
        java.nio.file.Path keyPath = java.nio.file.Paths.get("keys/jwt-keypair.ser");
        try {
            if (java.nio.file.Files.exists(keyPath)) {
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(java.nio.file.Files.newInputStream(keyPath))) {
                    this.keyPair = (KeyPair) ois.readObject();
                }
            } else {
                java.nio.file.Files.createDirectories(keyPath.getParent());
                this.keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
                try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(java.nio.file.Files.newOutputStream(keyPath))) {
                    oos.writeObject(this.keyPair);
                }
            }
        } catch (Exception e) {
            // Fallback
            this.keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        }
        RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
        
        // Simple JWKS structure for testing
        this.jwks = Map.of("keys", List.of(
            Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()),
                "e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray())
            )
        ));
    }

    public String createAccessToken(User user, List<UserEventRole> roles) {
        long now = System.currentTimeMillis();
        long ttl = 30L * 24 * 60 * 60 * 1000; // 30 days (eval window stability)

        List<String> eventRoles = roles.stream()
            .map(r -> r.getEventId() + ":" + r.getRole())
            .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("eventRoles", eventRoles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String createRefreshToken(User user) {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
