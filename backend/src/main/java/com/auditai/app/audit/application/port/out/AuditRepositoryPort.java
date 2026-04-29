package com.auditai.app.audit.application.port.out;

import com.auditai.app.audit.domain.Audit;

public interface AuditRepositoryPort {
  Audit save(Audit audit);
}
