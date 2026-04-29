package com.auditai.app.audit.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class CreateAuditServiceTest {

  @Mock
  private AuditRepositoryPort auditRepositoryPort;

  private CreateAuditService createAuditService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    Clock fixedClock = Clock.fixed(Instant.parse("2026-04-29T20:00:00Z"), ZoneOffset.UTC);
    createAuditService = new CreateAuditService(auditRepositoryPort, fixedClock);
  }

  @Test
  void shouldCreateAuditWithExpectedDefaults() {
    when(auditRepositoryPort.save(Mockito.any(Audit.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Audit result = createAuditService.create(new CreateAuditCommand("  time log content  "));

    ArgumentCaptor<Audit> captor = ArgumentCaptor.forClass(Audit.class);
    Mockito.verify(auditRepositoryPort).save(captor.capture());
    Audit saved = captor.getValue();

    assertNotNull(saved.getId());
    assertEquals("time log content", saved.getTimeLogContent());
    assertEquals(AuditStatus.PENDING, saved.getStatus());
    assertEquals(0, saved.getProcessingAttempts());
    assertEquals(LocalDateTime.of(2026, 4, 29, 20, 0, 0), saved.getCreatedAt());
    assertSame(saved, result);
  }
}
