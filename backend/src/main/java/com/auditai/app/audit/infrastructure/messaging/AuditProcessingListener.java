package com.auditai.app.audit.infrastructure.messaging;

import com.auditai.app.audit.application.exception.AiProviderUnavailableException;
import com.auditai.app.audit.application.service.AuditProcessingService;
import com.auditai.app.config.properties.AuditProcessingProperties;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProcessingListener {

  private final AuditProcessingService auditProcessingService;
  private final RabbitTemplate rabbitTemplate;
  private final AuditProcessingProperties properties;

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
      try {
        auditProcessingService.process(message.auditId());
      } catch (RuntimeException ex) {
        if (isFinalProcessingQueue(queue)) {
          republishToDlqWithErrorHeaders(message, ex);
          log.warn("audit_sent_to_dlq_with_error_headers auditId={} queue={}", message.auditId(), queue);
          return;
        }
        throw ex;
      }
    } finally {
      MDC.remove("requestId");
    }
  }

  private boolean isFinalProcessingQueue(String queue) {
    return properties.retryProcessingQueue1().equals(queue);
  }

  private void republishToDlqWithErrorHeaders(AuditProcessMessage message, RuntimeException ex) {
    rabbitTemplate.convertAndSend(
        properties.exchange(),
        properties.dlq(),
        message,
        outbound -> {
          outbound.getMessageProperties().setHeader("x-error-code", resolveErrorCode(ex));
          outbound.getMessageProperties().setHeader("x-error-message", safeErrorMessage(ex));
          outbound.getMessageProperties().setHeader("x-failed-at", OffsetDateTime.now().toString());
          return outbound;
        }
    );
  }

  private String resolveErrorCode(RuntimeException ex) {
    Throwable current = ex;
    while (current != null) {
      if (current instanceof AiProviderUnavailableException) {
        return "AI_PROVIDER_UNAVAILABLE";
      }
      current = current.getCause();
    }
    return "PROCESSING_ERROR";
  }

  private String safeErrorMessage(RuntimeException ex) {
    String message = ex.getMessage();
    if (message == null || message.isBlank()) {
      return "Unexpected processing error";
    }
    return message.length() > 500 ? message.substring(0, 500) : message;
  }
}
