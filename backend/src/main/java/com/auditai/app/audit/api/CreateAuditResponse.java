package com.auditai.app.audit.api;

import com.auditai.app.audit.domain.AuditStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAuditResponse(
    UUID id,
    AuditStatus status,
    LocalDateTime createdAt
) {
}
