package com.auditai.app.audit.infrastructure.messaging;

import com.auditai.app.audit.application.exception.AiProviderUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

public class AuditRabbitErrorHandler extends ConditionalRejectingErrorHandler {

  private static final Logger log = LoggerFactory.getLogger(AuditRabbitErrorHandler.class);

  @Override
  protected void log(Throwable throwable) {
    if (containsAiProviderUnavailable(throwable)) {
      log.warn("Rabbit listener failed due to temporary AI provider unavailability; message will follow broker retry/DLQ policy.");
      return;
    }
    super.log(throwable);
  }

  private boolean containsAiProviderUnavailable(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if (current instanceof AiProviderUnavailableException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
