package com.auditai.app.audit.infrastructure.ai;

import com.auditai.app.audit.application.port.out.AuditAiAnalyzerPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai.gemini", name = "enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAuditAiAnalyzer implements AuditAiAnalyzerPort {

  @Override
  public String analyze(String timeLogContent) {
    throw new IllegalStateException("AI analyzer is disabled. Set app.ai.gemini.enabled=true to process audits.");
  }
}
