package com.dogfood.event.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_invites", schema = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String token;
    private String email;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    private Boolean accepted;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
