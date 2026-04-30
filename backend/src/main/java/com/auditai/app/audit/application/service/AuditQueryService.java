package com.auditai.app.audit.application.service;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditNotFoundException;
import com.auditai.app.audit.domain.AuditStatus;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditQueryService {

  private final AuditRepositoryPort auditRepositoryPort;

  @Transactional(readOnly = true)
  public Audit getById(UUID id) {
    return auditRepositoryPort.findById(id).orElseThrow(() -> new AuditNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public List<Audit> list(AuditStatus status) {
    if (status == null) {
      return auditRepositoryPort.findAll();
    }
    return auditRepositoryPort.findByStatus(status);
  }
}
