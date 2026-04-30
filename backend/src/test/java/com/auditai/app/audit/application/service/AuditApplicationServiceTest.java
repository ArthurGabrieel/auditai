package com.auditai.app.audit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.port.in.view.CreateAuditView;
import com.auditai.app.audit.application.port.out.AuditProcessingPublisherPort;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class AuditApplicationServiceTest {

  @Mock
  private AuditRepositoryPort auditRepositoryPort;
  @Mock
  private AuditProcessingPublisherPort auditProcessingPublisherPort;

  private AuditApplicationService auditApplicationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Clock fixedClock = Clock.fixed(Instant.parse("2026-04-29T20:00:00Z"), ZoneOffset.UTC);
    auditApplicationService = new AuditApplicationService(
        auditRepositoryPort,
        auditProcessingPublisherPort,
        new SimpleMeterRegistry(),
        fixedClock
    );
  }

  @Test
  void shouldCreateAuditWithExpectedDefaults() {
    when(auditRepositoryPort.save(Mockito.any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CreateAuditView result = auditApplicationService.create(new CreateAuditCommand("  time log content  "));

    ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
    Mockito.verify(auditRepositoryPort).save(captor.capture());
    Audit saved = captor.getValue();

    assertNotNull(saved.getId());
    assertEquals("time log content", saved.getTimeLogContent());
    assertEquals(AuditStatus.PENDING, saved.getStatus());
    assertEquals(0, saved.getProcessingAttempts());
    assertEquals(LocalDateTime.of(2026, 4, 29, 20, 0, 0), saved.getCreatedAt());
    assertEquals(saved.getId(), result.id());
    assertEquals(saved.getStatus(), result.status());
    assertEquals(saved.getCreatedAt(), result.createdAt());
  }
}
