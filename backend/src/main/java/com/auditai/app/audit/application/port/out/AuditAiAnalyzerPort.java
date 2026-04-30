package com.auditai.app.audit.application.port.out;

public interface AuditAiAnalyzerPort {

  String analyze(String timeLogContent);
}
