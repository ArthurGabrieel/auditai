package com.auditai.app.audit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.auditai.app.audit.application.exception.AiProviderUnavailableException;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.application.port.out.AuditAiAnalyzerPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuditProcessingServiceTest {

  @Mock
  private AuditRepositoryPort auditRepositoryPort;
  @Mock
  private AuditAiAnalyzerPort auditAiAnalyzerPort;
  @Mock
  private AuditProcessingStateService auditProcessingStateService;

  private AuditProcessingService auditProcessingService;
  private SimpleMeterRegistry meterRegistry;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    meterRegistry = new SimpleMeterRegistry();
    auditProcessingService = new AuditProcessingService(
        auditRepositoryPort,
        auditProcessingStateService,
        auditAiAnalyzerPort,
        meterRegistry
    );
  }

  @Test
  void shouldProcessPendingAuditToCompleted() {
    UUID auditId = UUID.randomUUID();
    Audit pending = Audit.builder()
        .id(auditId)
        .timeLogContent("sample")
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(pending));
    when(auditRepositoryPort.save(any(Audit.class))).thenAnswer(inv -> inv.getArgument(0));
    when(auditAiAnalyzerPort.analyze(any(String.class))).thenReturn("analysis");

    auditProcessingService.process(auditId);

    verify(auditProcessingStateService, times(1)).markAttemptStarted(auditId);
    verify(auditProcessingStateService, times(1)).markCompleted(auditId, "analysis");
  }

  @Test
  void shouldSkipWhenAuditIsNotPending() {
    UUID auditId = UUID.randomUUID();
    Audit completed = Audit.builder()
        .id(auditId)
        .status(AuditStatus.COMPLETED)
        .processingAttempts(1)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(completed));

    auditProcessingService.process(auditId);

    verify(auditProcessingStateService, never()).markAttemptStarted(any(UUID.class));
  }

  @Test
  void shouldMarkAuditAsErrorWhenProcessingFails() {
    UUID auditId = UUID.randomUUID();
    Audit pending = Audit.builder()
        .id(auditId)
        .timeLogContent("sample")
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(pending));
    when(auditAiAnalyzerPort.analyze(any(String.class))).thenThrow(new RuntimeException("LLM timeout"));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> auditProcessingService.process(auditId));

    assertEquals("LLM timeout", exception.getMessage());
    verify(auditProcessingStateService, times(1)).markAttemptStarted(auditId);
    verify(auditProcessingStateService, times(1)).markError(auditId, "LLM timeout");
  }

  @Test
  void shouldExposeClearMessageAndMetricWhenAiProviderIsUnavailable() {
    UUID auditId = UUID.randomUUID();
    Audit pending = Audit.builder()
        .id(auditId)
        .timeLogContent("sample")
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(pending));
    when(auditAiAnalyzerPort.analyze(any(String.class)))
        .thenThrow(new AiProviderUnavailableException("503", new RuntimeException("provider unavailable")));

    RuntimeException exception = assertThrows(RuntimeException.class, () -> auditProcessingService.process(auditId));

    assertTrue(exception.getMessage().contains("temporarily unavailable"));
    assertEquals(1.0, meterRegistry.counter("audit_processing_ai_unavailable_total").count());
    assertEquals(1.0, meterRegistry.counter("audit_processing_error_total").count());
    verify(auditProcessingStateService, times(1)).markAttemptStarted(auditId);
    verify(auditProcessingStateService, times(1))
        .markPendingForRetry(auditId, "AI provider temporarily unavailable (Gemini 503). Message scheduled for retry.");
  }
}
