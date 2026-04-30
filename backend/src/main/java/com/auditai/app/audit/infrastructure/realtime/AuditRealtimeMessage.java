package com.auditai.app.audit.infrastructure.realtime;

import com.auditai.app.audit.domain.AuditStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditRealtimeMessage(
    UUID id,
    AuditStatus status,
    String aiOpinion,
    String errorReason,
    Integer processingAttempts,
    LocalDateTime completedAt
) {
}
