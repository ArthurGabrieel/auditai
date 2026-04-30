package com.auditai.app.audit.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import com.auditai.app.audit.application.service.AuditProcessingService;
import com.auditai.app.config.properties.AuditProcessingProperties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class AuditProcessingListenerTest {

  @Mock
  private AuditProcessingService auditProcessingService;
  @Mock
  private RabbitTemplate rabbitTemplate;

  private AuditProcessingListener listener;
  private AuditProcessingProperties properties;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    properties = new AuditProcessingProperties(
        "audit.exchange",
        "audit.process",
        "audit.process",
        "audit.process.r1",
        "audit.process.r1",
        "audit.process.dlq",
        "audit.retry.exchange",
        "audit.process.retry.1",
        "audit.process.retry.1",
        30000
    );
    listener = new AuditProcessingListener(auditProcessingService, rabbitTemplate, properties);
  }

  @Test
  void shouldProcessOnFirstAttemptWhenNoError() {
    UUID auditId = UUID.randomUUID();
    doNothing().when(auditProcessingService).process(auditId);

    listener.handle(new AuditProcessMessage(auditId), "audit.process");

    verify(auditProcessingService, times(1)).process(auditId);
    verify(rabbitTemplate, never()).convertAndSend(
        org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.<MessagePostProcessor>any()
    );
  }

  @Test
  void shouldPropagateExceptionForBrokerDrivenRetryBeforeFinalQueue() {
    UUID auditId = UUID.randomUUID();
    RuntimeException expected = new RuntimeException("always failing");
    org.mockito.Mockito.doThrow(expected).when(auditProcessingService).process(auditId);

    RuntimeException thrown = assertThrows(
        RuntimeException.class,
        () -> listener.handle(new AuditProcessMessage(auditId), "audit.process")
    );
    org.junit.jupiter.api.Assertions.assertSame(expected, thrown);
    verify(auditProcessingService, times(1)).process(auditId);
    verify(rabbitTemplate, never()).convertAndSend(
        org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.anyString(),
        org.mockito.ArgumentMatchers.any(),
        org.mockito.ArgumentMatchers.<MessagePostProcessor>any()
    );
  }

  @Test
  void shouldRepublishToDlqWithErrorHeadersOnFinalQueueFailure() {
    UUID auditId = UUID.randomUUID();
    RuntimeException expected = new RuntimeException("always failing");
    org.mockito.Mockito.doThrow(expected).when(auditProcessingService).process(auditId);

    listener.handle(new AuditProcessMessage(auditId), "audit.process.r1");

    verify(auditProcessingService, times(1)).process(auditId);
    verify(rabbitTemplate, times(1)).convertAndSend(
        org.mockito.ArgumentMatchers.eq("audit.exchange"),
        org.mockito.ArgumentMatchers.eq("audit.process.dlq"),
        org.mockito.ArgumentMatchers.eq(new AuditProcessMessage(auditId)),
        org.mockito.ArgumentMatchers.<MessagePostProcessor>any()
    );
  }
}
