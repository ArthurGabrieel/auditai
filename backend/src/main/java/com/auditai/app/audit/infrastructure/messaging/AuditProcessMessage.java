package com.auditai.app.audit.infrastructure.messaging;

import java.util.UUID;

public record AuditProcessMessage(UUID auditId) {
}
