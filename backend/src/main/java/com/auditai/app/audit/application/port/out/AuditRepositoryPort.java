package com.auditai.app.audit.application.port.out;

import com.auditai.app.audit.domain.Audit;
import java.util.Optional;
import java.util.UUID;

public interface AuditRepositoryPort {
  Audit save(Audit audit);
  Optional<Audit> findById(UUID id);
}
