package com.dogfood.observability.repository;
import com.dogfood.observability.entity.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface AuditRecordRepository extends JpaRepository<AuditRecord, UUID> {}
