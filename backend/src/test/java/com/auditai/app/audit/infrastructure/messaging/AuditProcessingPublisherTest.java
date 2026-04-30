package com.auditai.app.audit.infrastructure.messaging;

import static org.mockito.Mockito.verify;

import com.auditai.app.config.properties.AuditProcessingProperties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class AuditProcessingPublisherTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  private AuditProcessingPublisher auditProcessingPublisher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    AuditProcessingProperties properties = new AuditProcessingProperties(
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
    auditProcessingPublisher = new AuditProcessingPublisher(rabbitTemplate, properties);
  }

  @Test
  void shouldPublishMessageToConfiguredExchangeAndRoutingKey() {
    UUID auditId = UUID.randomUUID();

    auditProcessingPublisher.publish(auditId);

    verify(rabbitTemplate).convertAndSend(
        "audit.exchange",
        "audit.process",
        new AuditProcessMessage(auditId)
    );
  }
}
