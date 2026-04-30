package com.auditai.app.audit.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.auditai.app.audit.application.service.AuditProcessingService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuditProcessingListenerTest {

  @Mock
  private AuditProcessingService auditProcessingService;

  private AuditProcessingListener listener;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    listener = new AuditProcessingListener(auditProcessingService);
  }

  @Test
  void shouldProcessOnFirstAttemptWhenNoError() {
    UUID auditId = UUID.randomUUID();
    doNothing().when(auditProcessingService).process(auditId);

    listener.handle(new AuditProcessMessage(auditId), "audit.process");

    verify(auditProcessingService, times(1)).process(auditId);
  }

  @Test
  void shouldPropagateExceptionForBrokerDrivenRetry() {
    UUID auditId = UUID.randomUUID();
    RuntimeException expected = new RuntimeException("always failing");
    org.mockito.Mockito.doThrow(expected).when(auditProcessingService).process(auditId);

    RuntimeException thrown = assertThrows(
        RuntimeException.class,
        () -> listener.handle(new AuditProcessMessage(auditId), "audit.process")
    );
    org.junit.jupiter.api.Assertions.assertSame(expected, thrown);
    verify(auditProcessingService, times(1)).process(auditId);
  }
}
