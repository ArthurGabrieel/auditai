package com.auditai.app.audit.application.port.in;

import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.domain.Audit;

public interface CreateAuditUseCase {
  Audit create(CreateAuditCommand command);
}
