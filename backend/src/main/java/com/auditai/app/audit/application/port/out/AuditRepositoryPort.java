package com.auditai.app.audit.application.port.out;

import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditRepositoryPort {
  Audit save(Audit audit);
  Optional<Audit> findById(UUID id);
  List<Audit> findAll();
  List<Audit> findByStatus(AuditStatus status);
}
