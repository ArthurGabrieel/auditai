package com.auditai.app.audit.application.service;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingService {

  private final AuditRepositoryPort auditRepositoryPort;
  private final MeterRegistry meterRegistry;
  private final Clock clock;

  @Transactional
  public void process(UUID auditId) {
    Timer.Sample sample = Timer.start(meterRegistry);
    Audit audit = auditRepositoryPort.findById(auditId)
        .orElseThrow(() -> new IllegalStateException("Audit not found: " + auditId));

    if (audit.getStatus() != AuditStatus.PENDING) {
      log.info("audit_skipped_non_pending auditId={} status={}", auditId, audit.getStatus());
      return;
    }

    try {
      audit.setStatus(AuditStatus.PROCESSING);
      audit.setProcessingAttempts(audit.getProcessingAttempts() + 1);
      auditRepositoryPort.save(audit);

      // Placeholder for actual LLM analysis.
      audit.setAiOpinion("No critical issues found in initial automated review.");
      audit.setStatus(AuditStatus.COMPLETED);
      audit.setCompletedAt(LocalDateTime.now(clock));
      auditRepositoryPort.save(audit);

      meterRegistry.counter("audit_processing_success_total").increment();
      log.info("audit_processed auditId={} status={}", auditId, audit.getStatus());
    } catch (RuntimeException ex) {
      audit.setStatus(AuditStatus.ERROR);
      audit.setErrorReason(ex.getMessage());
      auditRepositoryPort.save(audit);
      meterRegistry.counter("audit_processing_error_total").increment();
      throw ex;
    } finally {
      sample.stop(meterRegistry.timer("audit_processing_duration_ms"));
    }
  }
}
