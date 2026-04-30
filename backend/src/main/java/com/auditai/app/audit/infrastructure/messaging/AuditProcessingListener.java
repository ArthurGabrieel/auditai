package com.auditai.app.audit.infrastructure.messaging;

import com.auditai.app.audit.application.service.AuditProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingListener {

  private final AuditProcessingService auditProcessingService;

  @RabbitListener(queues = {
      "${app.audit.processing.queue}",
      "${app.audit.processing.retry-processing-queue-1}"
  })
  public void handle(
      AuditProcessMessage message,
      @Header(name = AmqpHeaders.CONSUMER_QUEUE, required = false) String queue
  ) {
    String asyncRequestId = "amqp-" + message.auditId();
    MDC.put("requestId", asyncRequestId);
    try {
      log.info("audit_message_received auditId={} queue={}", message.auditId(), queue);
      auditProcessingService.process(message.auditId());
    } finally {
      MDC.remove("requestId");
    }
  }
}
