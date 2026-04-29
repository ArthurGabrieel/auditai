package com.auditai.app.audit.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuditRequest(
    @NotBlank(message = "timeLogContent is required")
    @Size(min = 1, max = 20000, message = "timeLogContent must have between 1 and 20000 characters")
    String timeLogContent
) {
}
