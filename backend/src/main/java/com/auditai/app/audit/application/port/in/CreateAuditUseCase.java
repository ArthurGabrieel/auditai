package com.auditai.app.audit.application.port.in;

import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.port.in.view.CreateAuditView;

public interface CreateAuditUseCase {
  CreateAuditView create(CreateAuditCommand command);
}
