package com.auditai.app.audit.domain;

import java.util.UUID;

public class AuditNotFoundException extends RuntimeException {
  public AuditNotFoundException(UUID id) {
    super("Audit not found: " + id);
  }
}
