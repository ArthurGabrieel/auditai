package com.auditai.app.audit.infrastructure.persistence.entity;

import com.auditai.app.audit.domain.AuditStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditJpaEntity {

  @Id
  private UUID id;

  @Column(name = "time_log_content", nullable = false, columnDefinition = "text")
  private String timeLogContent;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AuditStatus status;

  @Column(name = "ai_opinion", columnDefinition = "text")
  private String aiOpinion;

  @Column(name = "error_reason", columnDefinition = "text")
  private String errorReason;

  @Column(name = "processing_attempts", nullable = false)
  private Integer processingAttempts;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;
}
