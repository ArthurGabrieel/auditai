package com.auditai.app.audit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuditProcessingServiceTest {

  @Mock
  private AuditRepositoryPort auditRepositoryPort;

  private AuditProcessingService auditProcessingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Clock fixedClock = Clock.fixed(Instant.parse("2026-04-29T20:00:00Z"), ZoneOffset.UTC);
    auditProcessingService = new AuditProcessingService(
        auditRepositoryPort,
        new SimpleMeterRegistry(),
        fixedClock
    );
  }

  @Test
  void shouldProcessPendingAuditToCompleted() {
    UUID auditId = UUID.randomUUID();
    Audit pending = Audit.builder()
        .id(auditId)
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(pending));
    when(auditRepositoryPort.save(any(Audit.class))).thenAnswer(inv -> inv.getArgument(0));

    auditProcessingService.process(auditId);

    assertEquals(AuditStatus.COMPLETED, pending.getStatus());
    assertEquals(1, pending.getProcessingAttempts());
    assertEquals(LocalDateTime.of(2026, 4, 29, 20, 0), pending.getCompletedAt());
    verify(auditRepositoryPort, times(2)).save(any(Audit.class));
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

    verify(auditRepositoryPort, times(0)).save(any(Audit.class));
  }

  @Test
  void shouldMarkAuditAsErrorWhenProcessingFails() {
    UUID auditId = UUID.randomUUID();
    Audit pending = Audit.builder()
        .id(auditId)
        .status(AuditStatus.PENDING)
        .processingAttempts(0)
        .build();
    when(auditRepositoryPort.findById(auditId)).thenReturn(Optional.of(pending));
    when(auditRepositoryPort.save(any(Audit.class)))
        .thenReturn(pending)
        .thenThrow(new RuntimeException("LLM timeout"))
        .thenReturn(pending);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> auditProcessingService.process(auditId));

    assertEquals("LLM timeout", exception.getMessage());
    assertEquals(AuditStatus.ERROR, pending.getStatus());
    assertEquals("LLM timeout", pending.getErrorReason());
    verify(auditRepositoryPort, times(3)).save(any(Audit.class));
  }
}
