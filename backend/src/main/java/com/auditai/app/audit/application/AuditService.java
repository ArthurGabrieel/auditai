package com.auditai.app.audit.application;

import com.auditai.app.audit.api.CreateAuditRequest;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditRepository;
import com.auditai.app.audit.domain.AuditStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

  private final AuditRepository auditRepository;

  @Transactional
  public Audit create(CreateAuditRequest request) {
    Audit audit = new Audit();
    audit.setId(UUID.randomUUID());
    audit.setTimeLogContent(request.timeLogContent().trim());
    audit.setStatus(AuditStatus.PENDING);
    audit.setProcessingAttempts(0);
    audit.setCreatedAt(LocalDateTime.now());
    return auditRepository.save(audit);
  }
}
