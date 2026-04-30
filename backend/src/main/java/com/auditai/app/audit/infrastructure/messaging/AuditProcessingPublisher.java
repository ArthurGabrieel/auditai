package com.auditai.app.audit.infrastructure.messaging;

import com.auditai.app.audit.application.port.out.AuditProcessingPublisherPort;
import com.auditai.app.config.properties.AuditProcessingProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingPublisher implements AuditProcessingPublisherPort {

  private final RabbitTemplate rabbitTemplate;
  private final AuditProcessingProperties properties;

  @Override
  public void publish(UUID auditId) {
    rabbitTemplate.convertAndSend(
        properties.exchange(),
        properties.routingKey(),
        new AuditProcessMessage(auditId)
    );
    log.info("audit_enqueued auditId={} queue={}", auditId, properties.queue());
  }
}
