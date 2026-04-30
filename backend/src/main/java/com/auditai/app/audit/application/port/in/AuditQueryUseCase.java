package com.auditai.app.audit.application.port.in;

import com.auditai.app.audit.application.port.in.view.AuditView;
import com.auditai.app.audit.domain.AuditStatus;
import java.util.List;
import java.util.UUID;

public interface AuditQueryUseCase {

  AuditView getById(UUID id);

  List<AuditView> list(AuditStatus status);
}
