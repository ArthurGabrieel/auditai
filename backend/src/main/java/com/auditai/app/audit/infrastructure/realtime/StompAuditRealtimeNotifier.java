package com.auditai.app.audit.infrastructure.realtime;

import com.auditai.app.audit.application.port.out.AuditRealtimeNotifierPort;
import com.auditai.app.audit.domain.Audit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompAuditRealtimeNotifier implements AuditRealtimeNotifierPort {

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public void notifyAuditUpdated(Audit audit) {
    AuditRealtimeMessage message = new AuditRealtimeMessage(
        audit.getId(),
        audit.getStatus(),
        audit.getAiOpinion(),
        audit.getErrorReason(),
        audit.getProcessingAttempts(),
        audit.getCompletedAt()
    );
    messagingTemplate.convertAndSend("/topic/audits", message);
    messagingTemplate.convertAndSend("/topic/audits/" + audit.getId(), message);
    log.info("audit_realtime_published auditId={} status={}", audit.getId(), audit.getStatus());
  }
}
