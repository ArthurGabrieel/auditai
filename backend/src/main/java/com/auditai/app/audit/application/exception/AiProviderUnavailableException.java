package com.auditai.app.audit.application.exception;

public class AiProviderUnavailableException extends RuntimeException {

  public AiProviderUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
