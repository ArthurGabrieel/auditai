package com.auditai.app.audit.application.service;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.port.out.AuditProcessingPublisherPort;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateAuditService implements CreateAuditUseCase {

  private final AuditRepositoryPort auditRepositoryPort;
  private final AuditProcessingPublisherPort auditProcessingPublisherPort;
  private final MeterRegistry meterRegistry;
  private final Clock clock;

  @Override
  @Transactional
  public Audit create(CreateAuditCommand command) {
    Audit audit = Audit.builder()
        .id(UUID.randomUUID())
        .timeLogContent(command.timeLogContent().trim())
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .createdAt(LocalDateTime.now(clock))
        .build();
    Audit saved = auditRepositoryPort.save(audit);

    publishAfterCommit(saved.getId());
    
    Counter.builder("audits_created_total")
        .description("Number of audits created")
        .register(meterRegistry)
        .increment();
    log.info("audit_created auditId={} status={}", saved.getId(), saved.getStatus());
    return saved;
  }

  private void publishAfterCommit(UUID auditId) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          auditProcessingPublisherPort.publish(auditId);
        }
      });
      return;
    }
    auditProcessingPublisherPort.publish(auditId);
  }
}
