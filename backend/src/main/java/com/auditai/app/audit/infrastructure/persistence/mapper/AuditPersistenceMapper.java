package com.auditai.app.audit.infrastructure.persistence.mapper;

import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.infrastructure.persistence.entity.AuditJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditPersistenceMapper {

  public AuditJpaEntity toEntity(Audit audit) {
    return AuditJpaEntity.builder()
        .id(audit.getId())
        .timeLogContent(audit.getTimeLogContent())
        .status(audit.getStatus())
        .aiOpinion(audit.getAiOpinion())
        .errorReason(audit.getErrorReason())
        .processingAttempts(audit.getProcessingAttempts())
        .createdAt(audit.getCreatedAt())
        .completedAt(audit.getCompletedAt())
        .build();
  }

  public Audit toDomain(AuditJpaEntity entity) {
    return Audit.builder()
        .id(entity.getId())
        .timeLogContent(entity.getTimeLogContent())
        .status(entity.getStatus())
        .aiOpinion(entity.getAiOpinion())
        .errorReason(entity.getErrorReason())
        .processingAttempts(entity.getProcessingAttempts())
        .createdAt(entity.getCreatedAt())
        .completedAt(entity.getCompletedAt())
        .build();
  }
}
