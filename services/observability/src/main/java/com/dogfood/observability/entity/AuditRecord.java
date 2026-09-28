package com.dogfood.observability.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_records", schema = "audit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditRecord {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String eventType;
    private UUID entityId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;
    private OffsetDateTime createdAt;
}
