package com.auditai.app.audit.application.service;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditProcessingStateService {

  private final AuditRepositoryPort auditRepositoryPort;
  private final Clock clock;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAttemptStarted(UUID auditId) {
    Audit audit = load(auditId);
    audit.setStatus(AuditStatus.PROCESSING);
    audit.setProcessingAttempts(audit.getProcessingAttempts() + 1);
    auditRepositoryPort.save(audit);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markCompleted(UUID auditId, String aiOpinion) {
    Audit audit = load(auditId);
    audit.setAiOpinion(aiOpinion);
    audit.setStatus(AuditStatus.COMPLETED);
    audit.setCompletedAt(LocalDateTime.now(clock));
    auditRepositoryPort.save(audit);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markPendingForRetry(UUID auditId, String errorReason) {
    Audit audit = load(auditId);
    audit.setStatus(AuditStatus.PENDING);
    audit.setErrorReason(errorReason);
    auditRepositoryPort.save(audit);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markError(UUID auditId, String errorReason) {
    Audit audit = load(auditId);
    audit.setStatus(AuditStatus.ERROR);
    audit.setErrorReason(errorReason);
    auditRepositoryPort.save(audit);
  }

  private Audit load(UUID auditId) {
    return auditRepositoryPort.findById(auditId)
        .orElseThrow(() -> new IllegalStateException("Audit not found: " + auditId));
  }
}
