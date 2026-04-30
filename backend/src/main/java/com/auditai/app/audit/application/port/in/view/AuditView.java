package com.auditai.app.audit.application.port.in.view;

import com.auditai.app.audit.domain.AuditStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditView(
    UUID id,
    String timeLogContent,
    AuditStatus status,
    String aiOpinion,
    String errorReason,
    Integer processingAttempts,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {
}
