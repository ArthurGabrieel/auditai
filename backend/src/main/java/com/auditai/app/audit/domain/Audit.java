package com.auditai.app.audit.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
  private UUID id;
  private String timeLogContent;
  private AuditStatus status;
  private String aiOpinion;
  private String errorReason;
  private Integer processingAttempts;
  private LocalDateTime createdAt;
  private LocalDateTime completedAt;
}
