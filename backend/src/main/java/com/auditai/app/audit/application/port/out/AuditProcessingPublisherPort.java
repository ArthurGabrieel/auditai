package com.auditai.app.audit.application.port.out;

import java.util.UUID;

public interface AuditProcessingPublisherPort {
  void publish(UUID auditId);
}
