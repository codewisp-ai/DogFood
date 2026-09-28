package com.dogfood.observability.controller;

import com.dogfood.observability.entity.AuditRecord;
import com.dogfood.observability.repository.AuditRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRecordRepository auditRecordRepository;

    @GetMapping
    public ResponseEntity<Page<AuditRecord>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditRecordRepository.findAll(pageable));
    }
}
