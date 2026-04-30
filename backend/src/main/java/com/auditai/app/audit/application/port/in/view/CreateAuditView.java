package com.auditai.app.audit.application.port.in.view;

import com.auditai.app.audit.domain.AuditStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateAuditView(
    UUID id,
    AuditStatus status,
    LocalDateTime createdAt
) {
}
