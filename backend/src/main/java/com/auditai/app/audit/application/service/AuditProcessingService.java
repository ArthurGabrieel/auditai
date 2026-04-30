package com.auditai.app.audit.application.service;

import com.auditai.app.audit.application.exception.AiProviderUnavailableException;
import com.auditai.app.audit.application.port.out.AuditAiAnalyzerPort;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingService {

  private final AuditRepositoryPort auditRepositoryPort;
  private final AuditProcessingStateService auditProcessingStateService;
  private final AuditAiAnalyzerPort auditAiAnalyzerPort;
  private final MeterRegistry meterRegistry;

  public void process(UUID auditId) {
    Timer.Sample sample = Timer.start(meterRegistry);
    Audit audit = auditRepositoryPort.findById(auditId)
        .orElseThrow(() -> new IllegalStateException("Audit not found: " + auditId));

    if (audit.getStatus() != AuditStatus.PENDING) {
      log.info("audit_skipped_non_pending auditId={} status={}", auditId, audit.getStatus());
      return;
    }

    try {
      auditProcessingStateService.markAttemptStarted(auditId);

      String aiOpinion = auditAiAnalyzerPort.analyze(audit.getTimeLogContent());
      auditProcessingStateService.markCompleted(auditId, aiOpinion);

      meterRegistry.counter("audit_processing_success_total").increment();
      log.info("audit_processed auditId={} status={}", auditId, AuditStatus.COMPLETED);
    } catch (AiProviderUnavailableException ex) {
      String clearMessage = "AI provider temporarily unavailable (Gemini 503). Message scheduled for retry.";
      auditProcessingStateService.markPendingForRetry(auditId, clearMessage);
      meterRegistry.counter("audit_processing_ai_unavailable_total").increment();
      meterRegistry.counter("audit_processing_error_total").increment();
      log.warn("audit_processing_ai_unavailable auditId={} reason={}", auditId, clearMessage);
      throw new IllegalStateException(clearMessage, ex);
    } catch (RuntimeException ex) {
      auditProcessingStateService.markError(auditId, ex.getMessage());
      meterRegistry.counter("audit_processing_error_total").increment();
      throw ex;
    } finally {
      sample.stop(meterRegistry.timer("audit_processing_duration_ms"));
    }
  }
}
