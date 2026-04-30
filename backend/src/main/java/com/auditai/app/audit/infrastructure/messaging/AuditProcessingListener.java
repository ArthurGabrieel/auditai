package com.auditai.app.audit.infrastructure.messaging;

import com.auditai.app.audit.application.service.AuditProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingListener {

  private final AuditProcessingService auditProcessingService;

  @RabbitListener(queues = "${app.audit.processing.queue}")
  public void handle(AuditProcessMessage message) {
    log.info("audit_message_received auditId={}", message.auditId());
    int attempts = 0;
    long delayMs = 1000L;
    while (true) {
      attempts++;
      try {
        auditProcessingService.process(message.auditId());
        return;
      } catch (RuntimeException ex) {
        if (attempts >= 3) {
          log.error("audit_processing_failed auditId={} attempts={}", message.auditId(), attempts, ex);
          throw ex;
        }
        log.warn("audit_processing_retry auditId={} attempt={} nextDelayMs={}", message.auditId(), attempts, delayMs);
        try {
          Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("Retry interrupted", interruptedException);
        }
        delayMs *= 2;
      }
    }
  }
}
